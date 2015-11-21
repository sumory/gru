package com.sumory.gru.spear.extention;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQAck<T> implements IAck<T> {
    private final static Logger logger = LoggerFactory.getLogger(RabbitMQAck.class);

    private Channel channel;

    public RabbitMQAck(Channel ch) {
        this.channel = ch;
    }

    @Override
    public void ack(T d) {
        try {
            this.channel.basicAck(((QueueingConsumer.Delivery) d).getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("RabbitMQ ack错误", e);
        }
    }
}
