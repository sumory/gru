package com.sumory.gru.spear.server;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.sumory.gru.common.domain.StatObject;
import com.sumory.gru.common.utils.BitSetUtil;
import com.sumory.gru.common.utils.CollectionUtils;
import com.sumory.gru.common.utils.IdUtil;
import com.sumory.gru.common.utils.TokenUtil;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.*;
import com.sumory.gru.spear.transport.IReceiver;
import com.sumory.gru.spear.transport.ISender;
import com.sumory.gru.stat.service.StatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActionListener {
    private final Logger logger = LoggerFactory.getLogger(ActionListener.class);

    private final SpearContext context;
    private ConcurrentHashMap<String, Group> groupMap;//groupId - group
    private ConcurrentHashMap<String, User> userMap;//userId - user
    private StatService statService;
    private Map<String, String> config;
    private ISender sender;
    private IReceiver receiver;
    private String gruTopic = "gru_topic";

    public ActionListener(final SpearContext context) {
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
     * @param authObject
     * @return
     * @author sumory.wu @date 2015年4月22日 下午5:51:18
     */
    private boolean auth(AuthObject authObject) {
        logger.debug("鉴权");
        if (authObject.getId() == 0) {
            logger.error("鉴权失败，参数错误", authObject);
            return false;
        }

        if ("true".equals(this.config.get("auth.open"))) {
            String inputParams = authObject.getId() + "_" + authObject.getName() + "_" + authObject.getAppType();
            String genToken1 = TokenUtil.genToken(inputParams, config.get("salt.toticket"));
            String genToken2 = TokenUtil.genToken(inputParams + "_" + authObject.getToken1(), config.get("salt.tospear"));
            if (!authObject.getToken1().equals(genToken1) || !authObject.getToken2().equals(genToken2)) {
                logger.error("鉴权失败{}", authObject);
                return false;
            }
        }
        logger.debug("鉴权通过");
        return true;
    }

    /**
     * 判断是否已通过授权
     *
     * @param ioClient
     * @return
     * @author sumory.wu @date 2015年11月21日 下午2:58:29
     */
    private boolean checkAuth(SocketIOClient ioClient) {
        logger.debug("验证是否授权");
        Boolean authed = ioClient.get("auth");
        if (authed.booleanValue()) {
            logger.debug("授权通过，继续执行");
            return true;
        } else {
            logger.error("无授权非法操作{}", ioClient.getRemoteAddress());
            return false;
        }
    }

    @OnEvent("auth")
    public void onAuthEventHandler(SocketIOClient ioClient, String authStr, AckRequest ackRequest) {
        ioClient.set("auth", false);//将是否通过auth状态置为false

        logger.debug("授权信息, sessionId:{}, auth:{}, ip:{}", ioClient.getSessionId(),
                authStr, ioClient.getRemoteAddress());
        AuthObject authObject = JSONObject.parseObject(authStr, AuthObject.class);

        if (!auth(authObject)) {//鉴权
            ioClient.set("auth", false);
            CommonResult result = new CommonResult(false, ResultCode.PARAMS_ERROR, "无法通过权限验证");
            ackRequest.sendAckData(result);
            ioClient.disconnect();
            return;
        }

        //client id为节点内使用，只是用于区分不同client，所以不必依赖IdService
        Client newClient = new Client(IdUtil.generateClientId(), ioClient, context.getAck());
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
            } else {
                User existUser = userMap.get(key);
                ioClient.set("user", existUser);//为ioClient设置对应的user
                existUser.addClientToUser(newClient);
                logger.debug("已授权用户{}有新的client连入，当前client总数为{}", key, existUser
                        .getClients().size());//size操作耗时，生产去掉
            }
        }

        logger.debug("用户总数：{}", userMap.size());

        ioClient.set("auth", true);//验证通过
        CommonResult result = new CommonResult(true);
        ackRequest.sendAckData(result);
    }

    @OnEvent("subscribe")
    public void onSubscribeEventHandler(SocketIOClient ioClient, String subscribeStr, AckRequest ackRequest) {
        logger.debug("订阅信息, sessionId:{}, subscribe:{}, ip:{}", ioClient.getSessionId(),
                subscribeStr, ioClient.getRemoteAddress());
        boolean checkResult = checkAuth(ioClient);
        if (!checkResult) {
            logger.debug("无授权访问，断开连接: {}", ioClient.getRemoteAddress());
            ioClient.disconnect();
        }

        SubscribeObject sObject = JSONObject.parseObject(subscribeStr,
                SubscribeObject.class);
        List<SubscribeObject.SubscribeGroup> subscribeGroups = sObject.getSubscribeGroups();
        User u = (User) ioClient.get("user");//经过授权的该连接所属的user
        long userId = sObject.getUserId();//订阅信息中传过来的userId

        if (u == null || userId == 0 || u.getId() != userId) {
            CommonResult result = new CommonResult(false, ResultCode.DEFAULT_ERROR, "无法找到用户");
            ackRequest.sendAckData(result);
            return;
        }

        if (sObject.getUserId() == 0 || CollectionUtils.isEmpty(subscribeGroups)) {
            CommonResult result = new CommonResult(false, ResultCode.PARAMS_ERROR, "参数错误: 订阅的用户id为空或者订阅的群组为空");
            ackRequest.sendAckData(result);
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
                            } else {
                                u.getGroups().add(g);
                            }

                            g.addUserToGroup(u);
                        }
                    }
                } else {
                    logger.debug("{}群组不存在，初始化并添加用户{}", groupKey, userId);
                    Group newGroup = new Group();
                    newGroup.setId(sg.getId());
                    newGroup.setName(sg.getName());
                    groupMap.put(groupKey, newGroup);

                    newGroup.addUserToGroup(u);
                    if (CollectionUtils.isEmpty(u.getGroups())) {
                        List<Group> groups = new ArrayList<Group>(1);
                        groups.add(newGroup);
                        u.setGroups(groups);
                    } else {
                        u.getGroups().add(newGroup);
                    }
                }
            }
        }

        Map<String, Object> extraResult = new HashMap<String, Object>();
        extraResult.put("subscribeInfo", subscribeStr);
        CommonResult result = new CommonResult(true, "订阅群组消息成功", extraResult);
        ackRequest.sendAckData(result);
    }

    @OnEvent("msg")
    public void onMsgEventHandler(SocketIOClient ioClient, String data, AckRequest ackRequest) {
        logger.debug("收到信息, ioClient sessionId:{}, msg:{}", ioClient.getSessionId(), data);
        boolean checkResult = checkAuth(ioClient);
        if (!checkResult) {
            logger.debug("无授权访问，断开连接: {}", ioClient.getRemoteAddress());
            ioClient.disconnect();
        }

        try {
            final MsgObject msg = JSONObject.parseObject(data, MsgObject.class);
            User user = ioClient.get("user");//client对应的user
            msg.setFromId(user.getId());//设置发送者id

            int msgType = msg.getType();
            if (msgType == MsgObject.BRAODCAST.getValue()) {//群发
                logger.debug("来自用户{}的群播消息", user.getId());
            } else if (msgType == MsgObject.UNICAST.getValue()) {//单发
                logger.debug("来自用户{}-->用户{}的消息", user.getId(), msg.getTarget().get("id"));
            } else {
                CommonResult result = new CommonResult(false, ResultCode.PARAMS_ERROR, "消息类型不正确，请注明类型");
                ackRequest.sendAckData(result);//ack消息，告知客户端发生错误
                return;
            }

            logger.debug("消息发到队列, 来自userId:{}, userName:{}", user.getId(), user.getName());
            sender.send(gruTopic, msg);

            CommonResult result = new CommonResult(true, ResultCode.SUCCESS, "服务器已收到您发送的消息");
            ackRequest.sendAckData(result);//ack消息，告知客户端已收到
        } catch (Exception e) {
            CommonResult result = new CommonResult(false, ResultCode.SYSTEM_ERROR, "服务器处理消息发生异常，请检查");
            ackRequest.sendAckData(result);//ack消息，告知客户端异常
            logger.error("处理收到的消息出错", e);
        }
    }

    //获取在线人数
    @OnEvent("online")
    public void onOnlineEventHandler(SocketIOClient ioClient, String group, AckRequest ackRequest) {
        logger.debug(" 在线人数查询, ioClient sessionId: {}, 要查询的群组: {}",
                ioClient.getSessionId(), group);
        boolean checkResult = checkAuth(ioClient);
        if (!checkResult) {
            logger.debug("无授权访问，断开连接: {}", ioClient.getRemoteAddress());
            ioClient.disconnect();
        }

        try {
            Long groupId = Long.parseLong(group);//用户传过来的想要查询的group
            List<StatObject> stats = statService.getGroupStatObjectList(groupId);
            Set<Integer> userIds = new HashSet<Integer>();
            if (stats != null) {
                for (StatObject s : stats) {
                    if (s != null) {
                        s.setBitSet((BitSet) BitSetUtil.bytes2Object(s.getBitSetBytes()));
                        List<Integer> uIds = BitSetUtil.recoverFrom(s);
                        if (uIds != null && uIds.size() > 0)
                            userIds.addAll(uIds);
                    }
                }
            }
            logger.info("群组{}在线用户{}", groupId, userIds);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("userIds", userIds);
            CommonResult result = new CommonResult(true, ResultCode.SUCCESS, "", data);
            ackRequest.sendAckData(result);

        } catch (Exception e) {
            CommonResult result = new CommonResult(false, ResultCode.SYSTEM_ERROR, "服务器处理请求发生异常");
            ackRequest.sendAckData(result);
            logger.error("处理在线人数查询请求出错", e);
        }
    }

    @OnConnect
    public void onConnectHandler(SocketIOClient ioClient) {
        ioClient.set("auth", false);
        logger.debug("新用户登录:{}", ioClient.getSessionId());
    }

    @OnDisconnect
    public void onDisconnectHandler(SocketIOClient ioClient) {
        ioClient.set("auth", false);
        User u = ioClient.get("user");
        if (u != null) {
            synchronized (u) {
                if (u != null) {
                    logger.debug("用户:{} sessionId:{} 退出", u.getId(), ioClient.getSessionId());
                    u.removeClientFromUser(ioClient);
                    if (u.getClients().isEmpty()) {//如果user已经没有client连接了，说明user已经完全退出
                        logger.debug("用户{}的所有连接已退出，现在删除用户", u.getId());
                        userMap.remove(u.getId() + "");//从userMap中移除已经完全退出的user,fixbug: 必须传入的是string类型，否则删不掉

                        List<Group> joinedGroups = u.getGroups();
                        if (!CollectionUtils.isEmpty(joinedGroups)) {
                            logger.debug("要完全退出的用户{}加入的群组不为空，挨个从群组里删除该用户", u.getId());
                            for (Group g : joinedGroups) {
                                //kickGroup的操作会从groupMap删除某个group(锁不在user上)，所以这里得到的可能为空，须判断
                                if (g != null) {
                                    logger.debug("从群组挨个删除用户开始, groupId:{} userId:{}", g.getId(), u.getId());
                                    g.removeUserFromGroup(u);
                                } else {
                                    logger.debug("从群组挨个删除用户{}, 群组为null", u.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        ioClient.disconnect();
        ioClient = null;
    }
}
