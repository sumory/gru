package com.sumory.gru.ticket.server;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.sumory.gru.common.config.Config;
import com.sumory.gru.common.utils.TokenUtil;
import com.sumory.gru.ticket.common.Node;
import com.sumory.gru.ticket.domain.Ticket;
import com.sumory.gru.ticket.main.TicketMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    private HttpRequest request;
    private final String hello = "welcome, this is Ticket server...";

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //logger.info("HttpObject:{}", msg.toString());
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            URI uri = new URI(request.getUri());
            String uriPath = uri.getPath();

            // logger.info("REQUEST VERSION: " + request.getProtocolVersion().text());
            logger.info("REQUEST URL: " + request.getUri());
            logger.info("REQUEST METHOD: " + request.getMethod().toString());

            switch (uriPath) {
            case "/favicon.ico":
                return;
            case "/":
                response(ctx.channel(), hello, HttpResponseStatus.OK);
                return;
            case "/ticket":
                if (request.getMethod().equals(HttpMethod.GET)) {
                    QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
                    Map<String, List<String>> uriAttributes = decoderQuery.parameters();

                    String userType = null, userName = null, userId = null, token1 = null;
                    for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                        String key = attr.getKey();
                        List<String> values = attr.getValue();
                        String value = (values != null && values.size() > 0) ? attr.getValue().get(
                                0) : "";
                        if ("userType".equals(key)) {
                            userType = value;
                        }
                        if ("userName".equals(key)) {
                            userName = value;
                        }
                        else if ("userId".equals(key)) {
                            userId = value;
                        }
                        else if ("token1".equals(key)) {
                            token1 = value;
                        }
                        else {
                            logger.info("Illegal params: {} {} ", key, values);
                        }
                    }

                    if (StringUtils.isBlank(userName) || StringUtils.isBlank(userId)
                            || StringUtils.isBlank(token1)) {
                        Ticket ticket = new Ticket(false, 710, "参数不得为空", null);
                        responseJSON(ctx.channel(), JSON.toJSONString(ticket),
                                HttpResponseStatus.OK);
                        return;
                    }
                    else {
                        //鉴权
                        if ("true".equals(Config.get("auth.open"))) {
                            System.out.println("开启了鉴权");
                            String genToken = TokenUtil.genToken(userId + "_" + userName,
                                    Config.get("salt.toticket"));
                            if (!genToken.equals(token1)) {
                                Ticket ticket = new Ticket(false, 711, "token不合法", null);
                                responseJSON(ctx.channel(), JSON.toJSONString(ticket),
                                        HttpResponseStatus.OK);
                                return;
                            }
                        }

                        String toSpearToken = TokenUtil.genToken(userId + "_" + userName + "_"
                                + token1, Config.get("salt.tospear"));
                        Ticket ticket = new Ticket(false, 0, null, null);
                        try {
                            Node n = TicketMain.shard.getNode(request.toString());
                            if (n == null) {
                                ticket.setSuccess(false);
                                ticket.setErrorCode(701);
                                ticket.setMsg("无法找到任何长连接服务节点");
                            }
                            else {
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
                        }
                        catch (Exception e) {
                            ticket.setSuccess(false);
                            ticket.setErrorCode(750);
                            ticket.setMsg("获取长连接服务地址出现错误");
                        }
                        responseJSON(ctx.channel(), JSON.toJSONString(ticket),
                                HttpResponseStatus.OK);
                    }

                }
                else {//不是get请求,直接forbidden
                    response(ctx.channel(), "unsupported method type",
                            HttpResponseStatus.METHOD_NOT_ALLOWED);
                }
                break;
            default:
                response(ctx.channel(), "not found", HttpResponseStatus.NOT_FOUND);
                break;
            }
        }
        else {
            logger.error("非httprequest");
            response(ctx.channel(), "bad request", HttpResponseStatus.BAD_REQUEST);
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