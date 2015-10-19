package com.sumory.gru.spear.monitor;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOServer;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.Group;
import com.sumory.gru.spear.domain.User;
import com.sumory.gru.spear.transport.IReceiver;
import com.sumory.gru.spear.transport.ISender;

/**
 * <b>提供管理功能</b><br/>
 * 
 * 主要作为该spear节点的stub，向外提供本身状态<br/>
 * 这个功能应该只对服务管理员提供
 * 
 * @author sumory.wu
 * @date 2015年2月27日 下午5:53:59
 */
public class MonitorServer {
    private static Logger logger = LoggerFactory.getLogger(MonitorServer.class);

    private SpearContext context;
    private SocketIOServer server;

    private ConcurrentHashMap<String, Group> groupMap;//groupId - group
    private ConcurrentHashMap<String, User> userMap;//userId - group

    private ISender sender;
    private IReceiver receiver;

    public MonitorServer(SpearContext context) {
        this.context = context;
        this.groupMap = context.getGroupMap();
        this.userMap = context.getUserMap();
        this.sender = context.getSender();
        this.receiver = context.getReceiver();
    }

    public String getGroupMap() {
        return JSONObject.toJSONString(groupMap);
    }

    public void run() {
        //        Configuration config = new Configuration();
        //        String[] addrArray = this.context.getConfig().get("monitor.addr").split(":");
        //        config.setHostname(addrArray[0]);
        //        config.setPort(Integer.parseInt(addrArray[1]));
        //        SocketConfig socketConfig = new SocketConfig();
        //        socketConfig.setTcpKeepAlive(true);
        //        config.setSocketConfig(socketConfig);
        //        server = new SocketIOServer(config);
        //
        //        server.addEventListener("command", String.class, new SenderDataListener<String>(sender) {
        //            @Override
        //            public void onData(SocketIOClient client, String command, AckRequest ackRequest) {
        //                logger.debug("收到监控者发出的命令, ioClient:" + client.getSessionId() + " command:"
        //                        + command);
        //
        //            }
        //        });
        //
        //        //查看状态
        //        server.addEventListener("status", String.class, new SenderDataListener<String>(sender) {
        //
        //            @Override
        //            public void onData(SocketIOClient ioClient, String data, AckRequest ackRequest) {
        //                logger.debug("status请求, ioClient:" + ioClient.getSessionId() + " data:" + data);
        //
        //                ioClient.sendEvent("status", JSONObject.toJSONString(groupMap));
        //                ioClient.sendEvent("status", JSONObject.toJSONString(userMap));
        //                logger.debug("status请求，来自连接" + ioClient.getSessionId());
        //            }
        //        });
        //
        //        //强制群组下线
        //        server.addEventListener("kickGroup", String.class, new DataListener<String>() {
        //            @Override
        //            public void onData(SocketIOClient ioClient, String data, AckRequest ackRequest) {
        //                logger.debug("kickGroup请求, ioClient:" + ioClient.getSessionId() + " groupId:"
        //                        + data);
        //
        //                try {
        //                    long groupId = Long.parseLong(data);
        //                    Group group = groupMap.get(groupId);
        //
        //                    //1. 从userMap删除该群组下的所有user
        //                    ConcurrentLinkedQueue<User> users = group.getUsers();
        //                    Iterator<User> userIterator = users.iterator();
        //                    while (userIterator.hasNext()) {
        //                        User user = userIterator.next();
        //                        userMap.remove(user.getId());
        //                    }
        //
        //                    //2. 解散群组
        //                    group.dismiss();
        //
        //                    //3. groupMap中删除group
        //                    groupMap.remove(groupId);
        //                }
        //                catch (Exception e) {
        //                    logger.error("kickGroup请求处理异常", e);
        //                }
        //                logger.debug("kickGroup请求结束，来自连接" + ioClient.getSessionId());
        //            }
        //        });
        //
        //        //强制用户下线
        //        server.addEventListener("kickUser", String.class, new DataListener<String>() {
        //            @Override
        //            public void onData(SocketIOClient ioClient, String data, AckRequest ackRequest) {
        //                logger.debug("kickUser请求, ioClient:" + ioClient.getSessionId() + " userId:" + data);
        //
        //                try {
        //
        //                    //1. 剔除用户连接
        //                    long userId = Long.parseLong(data);
        //                    User user = userMap.get(userId);
        //                    user.dismiss();
        //
        //                    //2. 从userMap删除user
        //                    userMap.remove(userId);
        //
        //                    //3. 从groupMap对应群组中删除用户
        //                    long groupId = user.getGroupId();
        //                    logger.info("从group{}中移除user", groupId);
        //                    Group group = groupMap.get(groupId);
        //                    if (group != null) {
        //                        group.removeUserFromGrop(user);
        //                    }
        //                    else {
        //                        logger.warn("无法找到群组");
        //                    }
        //                }
        //                catch (Exception e) {
        //                    logger.error("kickUser请求处理异常", e);
        //                }
        //                logger.debug("kickUser请求结束，来自连接" + ioClient.getSessionId());
        //            }
        //        });
        //
        //        server.addEventListener("dumpUserMessage", String.class, new DataListener<String>() {
        //            @Override
        //            public void onData(SocketIOClient ioClient, String userId, AckRequest ackRequest) {
        //                logger.debug("dumpUserMessage,  userId:" + userId);
        //
        //                try {
        //
        //                }
        //                catch (Exception e) {
        //                    logger.error("dumpUserMessage请求处理异常", e);
        //                }
        //                logger.debug("dumpUserMessage请求结束，来自连接" + ioClient.getSessionId());
        //            }
        //        });
        //
        //        server.addConnectListener(new ConnectListener() {
        //            @Override
        //            public void onConnect(SocketIOClient client) {
        //                logger.debug("minions监控管理员登录:" + client.getSessionId());
        //            }
        //        });
        //
        //        server.addDisconnectListener(new DisconnectListener() {
        //            @Override
        //            public void onDisconnect(SocketIOClient ioClient) {
        //                try {
        //                    ioClient.disconnect();
        //                }
        //                catch (Exception e) {
        //                    logger.debug("手动调用client.disconnect出错");
        //                    e.printStackTrace();
        //                }
        //                logger.debug("minions监控管理员退出");
        //            }
        //        });
        //
        //        server.start();
        //        logger.info("MonitorServer is running...");
    }
}
