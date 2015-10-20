package com.sumory.gru.spear.transport.redis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.MsgObject;
import com.sumory.gru.spear.transport.ISender;

/**
 * 单节点时发送器
 * 
 * @author sumory.wu
 * @date 2015年10月15日 下午10:46:41
 */
public class RedisSender implements ISender {
    private final static Logger logger = LoggerFactory.getLogger(RedisSender.class);

    private SpearContext context;
    private BlockingQueue<MsgObject> msgQueue;//存放消息的队列
    private ExecutorService executor;

    public RedisSender(final SpearContext context) {
        this.context = context;
        this.msgQueue = this.context.getMsgQueue();
        this.executor = Executors.newScheduledThreadPool(1);

    }

    @Override
    public void send(String topic, long tags, String body) {

    }

    @Override
    public void send(final String topic, final MsgObject msg) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RedisListener.getInstance().publish(topic, msg);
                }
                catch (Exception e) {
                    logger.error("往redis消息队列{}传入消息发生异常", topic, e);
                }
            }
        });
    }

    @Override
    public void run() throws Exception {

    }

}
