package com.sumory.gru.spear.transport.rocketmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.sumory.gru.idgen.service.IdService;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.MsgObject;
import com.sumory.gru.spear.transport.ISender;

/**
 * mq消息发送器
 * 
 * @author sumory.wu
 * @date 2015年3月19日 下午3:04:23
 */
public class RocketMQSender implements ISender {
    private final static Logger logger = LoggerFactory.getLogger(RocketMQSender.class);

    private SpearContext context;
    private DefaultMQProducer producer;
    private IdService idService;

    public RocketMQSender(final SpearContext context) {
        this.context = context;
        this.idService = context.getIdService();
        //创建mq producer
        String mqNamesrvAddr = context.getConfig().get("mq.server.addr");
        this.producer = new DefaultMQProducer("SpearProducer");
        producer.setNamesrvAddr(mqNamesrvAddr);
    }

    public void run() throws Exception {
        this.producer.start();
    }

    //使用groupId当tags
    public void send(String topic, long tags, String body) {
        long msgId = 0;
        try {
            msgId = this.idService.getMsgId();
        }
        catch (Exception e) {
            logger.error("生成消息id异常", e);
        }

        try {
            Message msg = new Message(topic, tags + "", msgId + "", body.getBytes());
            SendResult result = this.producer.send(msg);
            logger.debug("{} 发送消息到队列, topic:{} tags:{}, 返回 id:{} result:{}", Thread.currentThread()
                    .getName(), topic, tags, result.getMsgId(), result.getSendStatus());
        }
        catch (Exception e) {
            logger.error("Sender发送异常", e);
        }
    }

    @Override
    public void send(final String topic, final MsgObject msg) {
        //TODO
    }
}

//Message msg = new Message("PushTopic", "push", "1", "Just for test.".getBytes());
//SendResult result = producer.send(msg);
//logger.debug("id:" + result.getMsgId() + " result:" + result.getSendStatus());
