package com.sumory.gru.spear.transport.inner;

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
public class InnerSender implements ISender {
    private final static Logger logger = LoggerFactory.getLogger(InnerSender.class);

    private SpearContext context;
    private BlockingQueue<MsgObject> msgQueue;//存放消息的队列
    private ExecutorService executor;

    public InnerSender(final SpearContext context) {
        this.context = context;
        this.msgQueue = this.context.getMsgQueue();
        this.executor = Executors.newFixedThreadPool(1);

    }

    @Override
    public void send(String topic, long tags, String body) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    MsgObject msg = new MsgObject();

                    InnerSender.this.msgQueue.put(msg);//put方法放入一个msg，若queue满了，等到queue有位置
                }
                catch (Exception e) {
                    logger.error("往内部消息队列传入消息发生异常", e);
                }
            }
        });
    }

    @Override
    public void send(final String topic, final MsgObject msg) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InnerSender.this.msgQueue.put(msg);//put方法放入一个msg，若queue满了，等到queue有位置
                }
                catch (Exception e) {
                    logger.error("往内部消息队列传入消息发生异常", e);
                }
            }
        });
    }

    @Override
    public void run() throws Exception {

    }

}
