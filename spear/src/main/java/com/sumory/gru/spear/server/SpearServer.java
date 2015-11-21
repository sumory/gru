package com.sumory.gru.spear.server;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.sumory.gru.spear.SpearContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 长连接接入server
 *
 * @author sumory.wu
 * @date 2015年10月17日 下午4:15:20
 */
public class SpearServer {
    private final static Logger logger = LoggerFactory.getLogger(SpearServer.class);

    private final SpearContext context;
    private SocketIOServer server;

    public SpearServer(final SpearContext context) {
        this.context = context;
    }

    public void run() {
        Configuration configuration = new Configuration();
        String[] addrArray = this.context.getConfig().get("spear.addr").split(":");
        configuration.setHostname(addrArray[0]);
        configuration.setPort(Integer.parseInt(addrArray[1]));

        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setTcpKeepAlive(true);
        socketConfig.setAcceptBackLog(1024);//accept队列长度，min(net.core.somaxconn, backlog)
        configuration.setSocketConfig(socketConfig);
        // setHeartbeatInterval Heartbeat interval (in seconds), defaults to 25
        // setHeartbeatTimeout Heartbeat timeout (in seconds), defaults to 60. Use 0 to disable it
        // setCloseTimeout Channel close timeout (in seconds) due to inactivity, defaults to 60

        server = new SocketIOServer(configuration);
        ActionListener actionListener = new ActionListener(context);
        server.addListeners(actionListener);
        server.start();
        logger.info("SpearServer is running...");

        //Configuration sConfiguration = server.getConfiguration();
        //logger.info("bossThreads:{}, workerThreads:{}, socketConfig:{}",
        //      sConfiguration.getBossThreads(), sConfiguration.getWorkerThreads(),
        //      JSON.toJSONString(sConfiguration.getSocketConfig(), true));

    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
