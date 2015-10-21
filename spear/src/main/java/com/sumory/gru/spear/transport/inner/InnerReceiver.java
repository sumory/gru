package com.sumory.gru.spear.transport.inner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.Group;
import com.sumory.gru.spear.domain.MsgObject;
import com.sumory.gru.spear.domain.User;
import com.sumory.gru.spear.message.BaseMessage;
import com.sumory.gru.spear.message.StringMessage;
import com.sumory.gru.spear.thread.ExecutesManager;
import com.sumory.gru.spear.transport.IReceiver;

/**
 * 单节点时接受器
 * 
 * @author sumory.wu
 * @date 2015年10月15日 下午10:46:02
 */
public class InnerReceiver implements IReceiver {

    private final static Logger logger = LoggerFactory.getLogger(InnerReceiver.class);

    private SpearContext context;

    private ConcurrentHashMap<String, Group> groupMap;//groupId - group
    private ConcurrentHashMap<String, User> userMap;//userId - user
    private BlockingQueue<MsgObject> msgQueue;//存放消息的队列

    private final ExecutesManager executesManager;

    public InnerReceiver(final SpearContext context) {
        this.context = context;
        this.groupMap = this.context.getGroupMap();
        this.userMap = this.context.getUserMap();
        this.msgQueue = this.context.getMsgQueue();

        int queueSize = 10000;
        int minCorePoolSize = 1;
        int maxCorePoolSize = 100;
        long keepAliveTime = 300L;
        this.executesManager = new ExecutesManager(minCorePoolSize, maxCorePoolSize, queueSize,
                keepAliveTime);
    }

    //内部传输不需要topic参数，留空即可
    @Override
    public void subscribe(String topic) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        MsgObject msg = msgQueue.take();//take方法取出一个，若为空，等到有为止(获取并移除此队列的头部)

                        logger.info("从消息队列取出消息发送：{}", msg);
                        InnerReceiver.this.consumeMessage(msg);
                    }
                }
                catch (Exception e) {
                    logger.error("消费内部消息队列发生异常", e);
                }
            }
        });

    }

    /**
     * 获取{@link Executor}用于安排执行任务
     * 
     * @author sumory.wu @date 2015年3月24日 上午9:14:41
     * @param serviceName
     * @return
     */
    public Executor getCallExecute(String serviceName) {
        return this.executesManager.getExecute(serviceName);
    }

    public void consumeMessage(final MsgObject m) {
        try {
            logger.debug("收到队列消息<--- thread:{} msg:{}", Thread.currentThread().getName(), m);
            InnerReceiver.this.getCallExecute("msg-sender").execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        int msgType = m.getType();//确定单播还是广播
                        String targetId = m.getTarget().get("id") + "";

                        Map<String, Object> target = new HashMap<String, Object>();
                        target.put("id", targetId);
                        target.put("type", -1);//扩展字段，暂时没用到
                        StringMessage sm = new StringMessage(0, m.getFromId(), msgType, target, m
                                .getContent());

                        if (msgType == MsgObject.BRAODCAST.getValue()) {//群发
                            sendToGroup(targetId, sm);
                        }
                        else if (msgType == MsgObject.UNICAST.getValue()) {//单发
                            sendToUser(targetId, sm);
                        }
                        else {
                            logger.error("接收到的要发送消息的类型错误");
                        }
                    }
                    catch (Exception e) {
                        logger.error("从队列接收消息后，异步发消息出错", e);
                    }
                }
            });
        }
        catch (Exception e) {
            logger.error("消费消息出错", e);
        }
    }

    /**
     * 群发给群组内所有人
     * 
     * @author sumory.wu @date 2015年4月24日 下午12:03:14
     * @param groupId
     * @param msg
     */
    private void sendToGroup(String groupId, BaseMessage msg) {
        logger.info("开始群发, groupId:{} msgId:{}", groupId, msg.getId());
        if (groupId != null) {
            Group group = this.groupMap.get(groupId);
            if (group != null)
                synchronized (group) {
                    group.broadcast("msg", msg);
                }
        }
    }

    /**
     * 发送给单个用户<br/>
     * 
     * @author sumory.wu @date 2015年10月17日 下午3:49:08
     * @param userId
     * @param msg
     */
    private void sendToUser(String userId, BaseMessage msg) {
        logger.info("开始单发, userId:{}  msgId:{}", userId, msg.getId());
        if (StringUtils.isBlank(userId))
            return;

        User u = this.userMap.get(userId);
        if (u == null || u.getClients() == null) {
            logger.debug("单发消息时无法获取到用户或者用户的clients为空, userId:{}", userId);
            return;
        }

        u.send("msg", msg);
    }
}
