package com.sumory.gru.ticket.server;

import com.alibaba.fastjson.JSON;
import com.sumory.gru.common.config.Config;
import com.sumory.gru.common.utils.TokenUtil;
import com.sumory.gru.ticket.common.Node;
import com.sumory.gru.ticket.domain.Ticket;
import com.sumory.gru.ticket.main.TicketMain;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class HttpHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    private HttpRequest request;
    private final String hello = "welcome, this is Ticket server...";

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //logger.info("HttpObject:{}", msg.toString());
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            URI uri = new URI(request.getUri());
            String uriPath = uri.getPath();

            logger.info("REQUEST METHOD:{} URL:{}", request.getMethod(), request.getUri());

            switch (uriPath) {
                case "/favicon.ico":
                    ctx.close();
                    return;
                case "/":
                    response(ctx.channel(), hello, HttpResponseStatus.OK);
                    return;
                case "/ticket":
                    handleTicketRequest(ctx, request);
                    break;
                default:
                    response(ctx.channel(), "not found", HttpResponseStatus.NOT_FOUND);
                    break;
            }
        } else {
            logger.error("非http request");
            response(ctx.channel(), "bad request", HttpResponseStatus.BAD_REQUEST);
        }
    }

    private void handleTicketRequest(ChannelHandlerContext ctx, HttpRequest request) {
        if (!request.getMethod().equals(HttpMethod.GET)) {//不是get请求,直接forbidden
            response(ctx.channel(), "unsupported method", HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        logger.info("请求ticket:{}", uriAttributes);

        String userId = null, userName = null, appType = null, token1 = null;
        for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            String key = attr.getKey();
            List<String> values = attr.getValue();
            String value = (values != null && values.size() > 0) ? attr.getValue().get(
                    0) : "";
            if ("userId".equals(key)) {
                userId = value;
            } else if ("userName".equals(key)) {
                userName = value;
            } else if ("appType".equals(key)) {
                appType = value;
            } else if ("token1".equals(key)) {
                token1 = value;
            } else {
                logger.info("Illegal params: {} {} ", key, values);
            }
        }

        if (StringUtils.isBlank(userName) || StringUtils.isBlank(userId) || StringUtils.isBlank(appType)
                || StringUtils.isBlank(token1)) {
            Ticket ticket = new Ticket(false, 710, "有必填参数为空", null);
            responseJSON(ctx.channel(), JSON.toJSONString(ticket), HttpResponseStatus.OK);
            return;
        } else {
            if ("true".equals(Config.get("auth.open"))) {//鉴权,验证token1
                String genToken = TokenUtil.genToken(userId + "_" + userName + "_" + appType,
                        Config.get("salt.toticket"));
                if (!genToken.equals(token1)) {
                    Ticket ticket = new Ticket(false, 711, "token不合法", null);
                    responseJSON(ctx.channel(), JSON.toJSONString(ticket), HttpResponseStatus.OK);
                    return;
                }
            }

            String toSpearToken = TokenUtil.genToken(userId + "_" + userName + "_" + appType + "_"
                    + token1, Config.get("salt.tospear"));
            Ticket ticket = new Ticket(false, 0, null, null);
            try {
                Node n = TicketMain.shard.getNode(userId);//按userId哈希
                if (n == null) {
                    ticket.setSuccess(false);
                    ticket.setErrorCode(701);
                    ticket.setMsg("无法找到任何长连接服务节点");
                } else {
                    ticket.setSuccess(true);
                    ticket.setErrorCode(0);
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    data.put("time", System.currentTimeMillis() / 1000);
                    data.put("addr", n.getAddr());
                    data.put("name", n.getName());
                    data.put("token", toSpearToken);
                    Iterator<Entry<String, Node>> it = TicketMain.shard.getShards()
                            .entrySet().iterator();
                    List<Node> backupNodes = new ArrayList<Node>();
                    while (it.hasNext()) {
                        Node backupNode = it.next().getValue();
                        if (!backupNode.getAddr().equals(n.getAddr())) {
                            backupNodes.add(backupNode);
                        }
                    }
                    data.put("backupNodes", backupNodes);
                    ticket.setData(data);
                }
            } catch (Exception e) {
                logger.error("获取长连接服务地址出现错误", e);
                ticket.setSuccess(false);
                ticket.setErrorCode(750);
                ticket.setMsg("获取长连接服务地址出现错误");
            }
            responseJSON(ctx.channel(), JSON.toJSONString(ticket), HttpResponseStatus.OK);
        }
    }

    private void response(Channel channel, String content, HttpResponseStatus httpResponseStatus) {
        ByteBuf buf = copiedBuffer(content, CharsetUtil.UTF_8);

        boolean close = request.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !request.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                httpResponseStatus, buf);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());//少不了
        }
        ChannelFuture future = channel.writeAndFlush(response);
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void responseJSON(Channel channel, String content, HttpResponseStatus httpResponseStatus) {
        ByteBuf buf = copiedBuffer(content, CharsetUtil.UTF_8);

        boolean close = request.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !request.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                httpResponseStatus, buf);
        response.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");

        if (!close) {
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());//少不了
        }
        ChannelFuture future = channel.writeAndFlush(response);
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}