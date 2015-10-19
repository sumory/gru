package com.sumory.gru.spear.mq;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

public class Producer implements Runnable {
    private int count;
    private String name;

    public Producer(int count, String name) {
        this.count = count;
        this.name = name;
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new Producer(100, "t1"));
        Thread t2 = new Thread(new Producer(10000, "t2"));
        t1.start();
        t2.start();
    }

    @Override
    public void run() {
        DefaultMQProducer producer = new DefaultMQProducer(this.name);
        producer.setNamesrvAddr("10.60.0.47:9876");
        long start = System.currentTimeMillis();
        try {
            producer.start();

            //            Message msg = new Message("PushTopic", "push", "1", "Just for test.".getBytes());
            //
            //            SendResult result = producer.send(msg);
            //            System.out.println("id:" + result.getMsgId() + " result:" + result.getSendStatus());
            //
            //            msg = new Message("PushTopic", "push", "2", "Just for test.".getBytes());
            //
            //            result = producer.send(msg);
            //            System.out.println("id:" + result.getMsgId() + " result:" + result.getSendStatus());
            //
            //            msg = new Message("PullTopic", "pull", "1", "Just for test.".getBytes());
            //
            //            result = producer.send(msg);
            //            System.out.println("id:" + result.getMsgId() + " result:" + result.getSendStatus());

            for (int i = 0; i < this.count; i++) {
                String body = "from " + this.name + " just a test[" + i + "]";
                Message msg = new Message("class", "10", "1", body.getBytes());
                SendResult result = producer.send(msg);
                //System.out.println("id:" + result.getMsgId() + " result:" + result.getSendStatus());
            }
            long end = System.currentTimeMillis();
            System.out.println("finish" + (end - start));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            producer.shutdown();
        }

    }
}