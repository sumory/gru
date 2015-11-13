package com.sumory.gru.ticket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;

import java.io.IOException;

import com.sumory.gru.common.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用一致性hash，防止抖动
 * 
 * @author sumory.wu
 * @date 2015年2月4日 上午10:07:08
 */
public class TicketServer {
//    static {
//        InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
//    }
//    private static final InternalLogger logger = InternalLoggerFactory
//            .getInstance(TicketServer.class);

    private static final Logger logger = LoggerFactory.getLogger(TicketServer.class);


    // 配置服务端的NIO线程组
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerBootstrap b = new ServerBootstrap();
    private Channel channel;

    public TicketServer() {
    }

    public void startHttp() throws Exception {
        logger.info("start http service");
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024);

        b.childHandler(new ChannelInitializer<SocketChannel>() {
            // 创建一个5个线程的线程组来处理耗时的业务逻辑  
            // private EventExecutorGroup group = new DefaultEventExecutorGroup(5);

            @Override
            public void initChannel(SocketChannel ch) throws IOException {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                //pipeline.addLast("servercodec", new HttpServerCodec());
                pipeline.addLast("aggegator", new HttpObjectAggregator(1024 * 1024 * 64));//定义缓冲数据量  

                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
                // pipeline.addLast("deflater", new HttpContentCompressor());
                pipeline.addLast("httpHandler", new HttpHandler());
                // 将SlowBusinessHandler中的业务逻辑放到EventExecutorGroup线程组中执行  
                //pipeline.addLast(group, new SlowBusinessHandler());
            }
        });

        // 绑定端口，同步等待成功
        ChannelFuture f = b.bind(Config.get("ticket.hostname"),
                Integer.parseInt(Config.get("ticket.port")));
        f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("client connected →");
                    channel = future.channel();
                }
                else {
                    logger.error("server start failed");
                    future.cause().printStackTrace();
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutdown callback is invoked.");
                if (channel != null && channel.isOpen()) {
                    channel.close();
                }
                logger.info("shutdown bossGroup and workerGroup");
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
    }

}
