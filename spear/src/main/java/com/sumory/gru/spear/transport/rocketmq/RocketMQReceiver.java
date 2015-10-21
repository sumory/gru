package com.sumory.gru.spear.transport.rocketmq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.Group;
import com.sumory.gru.spear.domain.MsgObject;
import com.sumory.gru.spear.domain.User;
import com.sumory.gru.spear.message.BaseMessage;
import com.sumory.gru.spear.message.StringMessage;
import com.sumory.gru.spear.thread.ExecutesManager;
import com.sumory.gru.spear.transport.IReceiver;

/**
 * mq消息消费
 * 
 * @author sumory.wu
 * @date 2015年3月19日 下午3:04:05
 */
public class RocketMQReceiver implements IReceiver {
    private final static Logger logger = LoggerFactory.getLogger(RocketMQReceiver.class);

    private SpearContext context;
    private String mqNamesrvAddr;
    private ConcurrentHashMap<String, DefaultMQPushConsumer> consumersMap;

    private ConcurrentHashMap<String, Group> groupMap;
    private ConcurrentHashMap<String, User> userMap;//userType_userId - user

    private final ExecutesManager executesManager;

    public RocketMQReceiver(final SpearContext context) {
        this.context = context;
        this.groupMap = context.getGroupMap();
        this.userMap = context.getUserMap();
        this.mqNamesrvAddr = context.getConfig().get("mq.server.addr");
        this.consumersMap = new ConcurrentHashMap<String, DefaultMQPushConsumer>();

        int queueSize = 10000;
        int minCorePoolSize = 1;
        int maxCorePoolSize = 100;
        long keepAliveTime = 300L;
        this.executesManager = new ExecutesManager(minCorePoolSize, maxCorePoolSize, queueSize,
                keepAliveTime);
    }

    /**
     * 创建topic，订阅消息
     * 
     * @author sumory.wu @date 2015年1月16日 下午6:59:44
     * @param topic "class"
     * @param subExpression classId
     */
    @Override
    public void subscribe(final String topic) {
        try {
            synchronized (consumersMap) {//同步避免采坑，fix bug:多个订阅topic时会导致并发错误
                logger.info("订阅消息, topic:{}", topic);
                String key = topic;//后期优化后使用单topic，多tag来区分

                if (consumersMap.containsKey(key)) {
                    logger.info("已订阅{},直接返回", key);
                    return;//已订阅
                }

                logger.info("未订阅{},开始订阅", key);
                DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("SpearConsumer");
                consumer.setNamesrvAddr(mqNamesrvAddr);
                consumer.setMessageModel(MessageModel.BROADCASTING);
                consumer.subscribe(key, "*");//订阅PushTopic下Tag为push的消息
                //一个新的订阅组第一次启动从队列的最后位置开始消费
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
                consumer.registerMessageListener(new SpearMessageListener());//自定义的listener实现

                consumersMap.put(key, consumer);
                consumer.start();
            }
        }
        catch (Exception e) {
            logger.error("订阅消息异常", e);
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
     * @author sumory.wu @date 2015年4月24日 上午11:55:15
     * @param userId
     * @param msg
     */
    private void sendToUser(String userId, BaseMessage msg) {
        logger.info("开始单发, userId:{} msgId:{}", userId, msg.getId());
        if (userId == null)
            return;

        User u = this.userMap.get(userId.toString());
        if (u != null) {
            logger.debug("发给用户");
            u.send("msg", msg);
        }
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

    /**
     * 消息消费监听器
     * 
     * @author sumory.wu
     * @date 2015年4月24日 上午11:43:09
     */
    private class SpearMessageListener implements MessageListenerOrderly {

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs,
                ConsumeOrderlyContext context) {
            final Message msg = msgs.get(0);
            try {//要求不得抛出异常，这里try{}catch掉
                logger.debug("收到队列消息<--- thread:{} msg:{}", Thread.currentThread().getName(), msg);
                RocketMQReceiver.this.getCallExecute("msg-sender").execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String tags = msg.getTags();//这里是群组id
                            // Long groupId = Long.parseLong(tags);
                            // String msgId = msg.getKeys();
                            String body = new String(msg.getBody(), "utf8");
                            logger.info("收到的消息属性, topic:{} tags:{} body:{}", msg.getTopic(), tags,
                                    body);
                            MsgObject m = JSONObject.parseObject(body, MsgObject.class);

                            int msgType = m.getType();//确定单播还是广播
                            String targetId = m.getTarget().get("id") + "";

                            Map<String, Object> target = new HashMap<String, Object>();
                            target.put("id", targetId);
                            target.put("type", -1);//扩展字段，暂时没用到
                            StringMessage sm = new StringMessage(0, m.getFromId(), msgType, target,
                                    m.getContent());

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
                            logger.error("异步发消息出错", e);
                        }
                    }
                });
            }
            catch (Exception e) {
                logger.error("消费消息出错", e);
            }

            return ConsumeOrderlyStatus.SUCCESS;
        }
    }
}
