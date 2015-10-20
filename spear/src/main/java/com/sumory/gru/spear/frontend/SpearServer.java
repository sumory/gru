package com.sumory.gru.spear.frontend;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.sumory.gru.common.domain.StatObject;
import com.sumory.gru.common.utils.BitSetUtil;
import com.sumory.gru.common.utils.CollectionUtils;
import com.sumory.gru.common.utils.IdUtil;
import com.sumory.gru.common.utils.TokenUtil;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.AuthObject;
import com.sumory.gru.spear.domain.Client;
import com.sumory.gru.spear.domain.CommonResult;
import com.sumory.gru.spear.domain.Group;
import com.sumory.gru.spear.domain.MsgObject;
import com.sumory.gru.spear.domain.SubscribeObject;
import com.sumory.gru.spear.domain.User;
import com.sumory.gru.spear.extention.ReceiverDataListener;
import com.sumory.gru.spear.extention.SenderDataListener;
import com.sumory.gru.spear.transport.IReceiver;
import com.sumory.gru.spear.transport.ISender;
import com.sumory.gru.stat.service.StatService;

/**
 * * 长连接接入server
 * 
 * <p>
 * <b>TODO:</b>
 * <p/>
 * <ul>
 * <li>每个用户级别应该有有限个客户端连接</li>
 * </ul>
 * 
 * 
 * @author sumory.wu
 * @date 2015年10月17日 下午4:15:20
 */
public class SpearServer {
    private final static Logger logger = LoggerFactory.getLogger(SpearServer.class);

    private SpearContext context;
    private SocketIOServer server;

    private ConcurrentHashMap<String, Group> groupMap;//groupId - group
    private ConcurrentHashMap<String, User> userMap;//userId - user
    private StatService statService;
    private Map<String, String> config;
    private ISender sender;
    private IReceiver receiver;
    private String gruTopic = "gru_topic";

    public SpearServer(final SpearContext context) {
        this.context = context;
        this.groupMap = context.getGroupMap();
        this.userMap = context.getUserMap();
        this.config = context.getConfig();
        this.sender = context.getSender();
        this.receiver = context.getReceiver();
        this.statService = context.getStatService();

        this.receiver.subscribe(gruTopic);
    }

    /**
     * 鉴权
     * 
     * @author sumory.wu @date 2015年4月22日 下午5:51:18
     * @param authObject
     * @return
     */
    private boolean checkAuth(AuthObject authObject) {
        logger.debug("鉴权");
        if (authObject.getId() == 0) {
            logger.error("鉴权失败，参数错误", authObject);
            return false;
        }

        if ("true".equals(this.config.get("auth.open"))) {
            String genToken1 = TokenUtil.genToken(authObject.getId() + "_" + authObject.getName(),
                    config.get("salt.toticket"));
            String genToken2 = TokenUtil.genToken(authObject.getId() + "_" + authObject.getName()
                    + "_" + authObject.getToken1(), config.get("salt.tospear"));
            if (!authObject.getToken1().equals(genToken1)
                    || !authObject.getToken2().equals(genToken2)) {
                logger.error("鉴权失败", authObject);
                return false;
            }
        }
        logger.debug("鉴权通过");
        return true;
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

        //鉴权
        server.addEventListener("auth", String.class, new ReceiverDataListener<String>(receiver) {
            @Override
            public void onData(SocketIOClient ioClient, String authStr, AckRequest ackRequest) {
                ioClient.set("auth", false);//将是否通过auth状态置为false

                logger.debug("授权信息, sessionId:{}, auth:{}, ip:{}", ioClient.getSessionId(),
                        authStr, ioClient.getRemoteAddress());
                AuthObject authObject = JSONObject.parseObject(authStr, AuthObject.class);

                if (!checkAuth(authObject)) {//鉴权
                    ioClient.disconnect();
                    return;
                }

                //client id为节点内使用，只是用于区分不同client，所以不必依赖IdService
                Client newClient = new Client(IdUtil.generateClientId(), ioClient);

                synchronized (userMap) {
                    String key = authObject.getId() + "";//用户的id作为key
                    boolean isUserExist = userMap.containsKey(key);

                    if (!isUserExist) {
                        User newUser = new User(authObject.getId());
                        newUser.setName(authObject.getName());
                        newUser.addClientToUser(newClient);
                        ioClient.set("user", newUser);//为ioClient设置对应的user
                        logger.debug("新授权用户{}的client总数为{}", key, newUser.getClients().size());//size操作耗时，生产去掉
                        userMap.put(key, newUser);
                    }
                    else {
                        User existUser = userMap.get(key);
                        ioClient.set("user", existUser);//为ioClient设置对应的user
                        existUser.addClientToUser(newClient);
                        logger.debug("已授权用户{}有新的client连入，当前client总数为{}", key, existUser
                                .getClients().size());//size操作耗时，生产去掉
                    }
                }

                logger.debug("用户总数：{}", userMap.size());

                ioClient.set("auth", true);//验证通过
                CommonResult cr = new CommonResult(true, 0, null, null);
                ioClient.sendEvent("auth_result", cr);
            }
        });

        server.addEventListener("subscribe", String.class, new ReceiverDataListener<String>(
                receiver) {
            @Override
            public void onData(SocketIOClient ioClient, String subscribeStr, AckRequest ackRequest) {
                logger.debug("订阅信息, sessionId:{}, subscribe:{}, ip:{}", ioClient.getSessionId(),
                        subscribeStr, ioClient.getRemoteAddress());
                SubscribeObject sObject = JSONObject.parseObject(subscribeStr,
                        SubscribeObject.class);
                List<SubscribeObject.SubscribeGroup> subscribeGroups = sObject.getSubscribeGroups();
                User u = (User) ioClient.get("user");//经过授权的该连接所属的user
                long userId = sObject.getUserId();//订阅信息中传过来的userId

                if (u == null || userId == 0 || u.getId() != userId) {
                    CommonResult cr = new CommonResult(false, 0, "无法找到用户", null);
                    ioClient.sendEvent("subscribe_result", cr);
                    return;
                }

                if (sObject.getUserId() == 0 || CollectionUtils.isEmpty(subscribeGroups)) {
                    CommonResult cr = new CommonResult(false, 0, "参数错误: 订阅的用户id为空或者订阅的群组为空", null);
                    ioClient.sendEvent("subscribe_result", cr);
                    return;
                }

                synchronized (groupMap) {
                    for (SubscribeObject.SubscribeGroup sg : subscribeGroups) {
                        String groupKey = sg.getId() + "";
                        Group g = groupMap.get(groupKey);//说明此群组在当前进程中存在（有人订阅过这个群组了）

                        if (g != null) {
                            logger.debug("{}群组已存在，添加该用户{}", groupKey, userId);
                            synchronized (g) {
                                ConcurrentLinkedQueue<User> users = g.getUsers();
                                boolean isUserExist = false;//用户是否存在于群组中
                                Iterator<User> iterator = users.iterator();
                                while (iterator.hasNext()) {
                                    User uu = iterator.next();
                                    if (u.getId() == uu.getId()) {//说明用户已存在
                                        isUserExist = true;
                                        break;
                                    }
                                }
                                if (!isUserExist) {
                                    if (CollectionUtils.isEmpty(u.getGroups())) {
                                        List<Group> groups = new ArrayList<Group>(1);
                                        groups.add(g);
                                        u.setGroups(groups);
                                    }
                                    else {
                                        u.getGroups().add(g);
                                    }

                                    g.addUserToGroup(u);
                                }
                            }
                        }
                        else {
                            logger.debug("{}群组不存在，初始化并添加用户{}", groupKey, userId);
                            Group newGroup = new Group();
                            newGroup.setId(sg.getId());
                            newGroup.setName(sg.getName());
                            groupMap.put(groupKey, newGroup);

                            newGroup.addUserToGroup(u);
                            if (CollectionUtils.isEmpty(u.getGroups())) {
                                List<Group> groups = new ArrayList<Group>(1);
                                groups.add(g);
                                u.setGroups(groups);
                            }
                            else {
                                u.getGroups().add(g);
                            }
                        }
                    }
                }

                Map<String, Object> extraResult = new HashMap<String, Object>();
                extraResult.put("subscribeinfo", subscribeStr);
                CommonResult cr = new CommonResult(true, 0, "订阅群组消息成功", extraResult);
                ioClient.sendEvent("subscribe_result", cr);
            }
        });

        //通用消息通道
        server.addEventListener("msg", String.class, new SenderDataListener<String>(sender) {
            //需判断是否已经是授权过的连接发送的信息，是否需要另加token？
            @Override
            public void onData(SocketIOClient ioClient, String data, AckRequest ackRequest) {
                logger.debug("收到信息, ioClient sessionId: {}, msg: {}", ioClient.getSessionId(), data);
                Map<String, Object> result = new HashMap<String, Object>();
                try {
                    MsgObject msg = JSONObject.parseObject(data, MsgObject.class);
                    User user = ioClient.get("user");//client对应的user

                    int msgType = msg.getType();
                    if (msgType == MsgObject.BRAODCAST.getValue()) {//群发
                        logger.debug("来自用户{}的群播消息", user.getId());
                    }
                    else if (msgType == MsgObject.UNICAST.getValue()) {//单发
                        logger.debug("来自用户{}的单播到用户{}的消息", user.getId(), msg.getTarget().get("id"));
                    }
                    else {
                        result.put("success", false);
                        result.put("errorCode", -2);
                        result.put("msg", "消息类型不正确，请注明类型");
                        ioClient.sendEvent("msg_result", result);
                        return;
                    }

                    logger.debug("发到队列消息---->：来自userId: {}, userName: {}", user.getId(),
                            user.getName());

                    //sender.send("class", 0, data);
                    sender.send(gruTopic, msg);

                    result.put("success", true);
                    result.put("errorCode", 0);
                    result.put("msg", "服务器已收到您发送的消息");
                    ioClient.sendEvent("msg_result", result);
                }
                catch (Exception e) {
                    result.put("success", false);
                    result.put("errorCode", -1);
                    result.put("msg", "服务器处理消息发生异常，请检查");
                    ioClient.sendEvent("msg_result", result);
                    logger.error("处理收到的消息出错", e);
                }
            }
        });

        //获取在线人数
        server.addEventListener("online", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient ioClient, String group, AckRequest ackRequest) {
                logger.debug(" 在线人数查询, ioClient sessionId: {}, 要查询的群组: {}",
                        ioClient.getSessionId(), group);
                Map<String, Object> result = new HashMap<String, Object>();
                String onlineResultEvent = "online_result";
                try {
                    Long groupId = Long.parseLong(group);//用户传过来的想要查询的group

                    List<StatObject> stats = statService.getGroupStatObjectList(groupId);
                    Set<Integer> userIds = new HashSet<Integer>();
                    List<User> tachers = new ArrayList<User>();
                    for (StatObject s : stats) {
                        if (s != null) {
                            List<User> ts = JSON.parseArray(s.getExtra(), User.class);
                            tachers.addAll(ts);
                            s.setBitSet((BitSet) BitSetUtil.bytes2Object(s.getBitSetBytes()));

                            List<Integer> uIds = BitSetUtil.recoverFrom(s);
                            if (uIds != null && uIds.size() > 0)
                                userIds.addAll(uIds);
                        }
                    }

                    logger.info("在线用户{}", userIds);
                    result.put("success", true);
                    result.put("errorCode", 0);
                    result.put("msg", "");
                    result.put("data", userIds);
                    result.put("teachers", tachers);
                    ioClient.sendEvent(onlineResultEvent, result);
                }
                catch (Exception e) {
                    result.put("success", false);
                    result.put("errorCode", -1);
                    result.put("msg", "服务器处理请求发生异常，请检查");
                    ioClient.sendEvent(onlineResultEvent, result);
                    logger.error("处理在线人数查询请求出错", e);
                }
            }
        });

        //监听连接建立
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                logger.debug("新用户登录:" + client.getSessionId());
            }
        });

        //监听连接断开
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient ioClient) {
                User u = ioClient.get("user");
                try {
                    ioClient.disconnect();
                }
                catch (Exception e) {
                    logger.error("手动调用client.disconnect出错", e);
                }

                if (u != null) {
                    logger.debug("用户:{} sessionId:{} 退出", u.getId(), ioClient.getSessionId());

                    synchronized (u) {
                        u.removeClientFromUser(ioClient);
                        if (u.getClients().isEmpty()) {//如果user已经没有client连接了，说明user已经完全退出
                            logger.debug("用户{}的所有连接已退出，现在删除用户", u.getId());
                            userMap.remove(u.getId() + "");//从userMap中移除已经完全退出的user,fixbug: 必须传入的是string类型，否则删不掉

                            List<Group> joinedGroups = u.getGroups();
                            if (!CollectionUtils.isEmpty(joinedGroups)) {
                                for (Group g : joinedGroups) {
                                    //直接kickGroup的操作直接会从groupMap删除某个group，所以这里得到的可能为空
                                    if (g != null) {
                                        g.removeUserFromGrop(u);
                                    }
                                }
                            }
                        }
                    }
                }
                ioClient = null;
            }
        });

        server.start();
        Configuration sConfiguration = server.getConfiguration();
        logger.info("bossThreads:{}, workerThreads:{}, socketConfig:{}",
                sConfiguration.getBossThreads(), sConfiguration.getWorkerThreads(),
                JSON.toJSONString(sConfiguration.getSocketConfig(), true));
        logger.info("SpearServer is running...");
    }
}
