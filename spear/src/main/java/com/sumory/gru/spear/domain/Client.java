package com.sumory.gru.spear.domain;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.sumory.gru.spear.extention.IAck;
import com.sumory.gru.spear.extention.LogAck;
import com.sumory.gru.spear.extention.RabbitMQAck;
import com.sumory.gru.spear.message.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 一个client对应一个连接实体，比如一个手机端连接，一个chrome tab页里的连接等
 *
 * @author sumory.wu
 * @date 2015年3月13日 下午3:13:46
 */
public class Client {
    private final static Logger logger = LoggerFactory.getLogger(Client.class);

    public IAck ack;
    public boolean needAck;
    public long id;
    public UUID uuid;
    public transient SocketIOClient ioClient;


    public Client() {
        this.needAck = false;
    }

    public Client(IAck ack) {
        this.ack = ack;
        this.needAck = true;
    }

    public Client(long id, SocketIOClient ioClient, IAck ack) {
        this.ioClient = ioClient;
        this.uuid = this.ioClient.getSessionId();
        this.id = id;

        if (ack != null) {
            this.ack = ack;
            this.needAck = true;
        } else {
            this.needAck = false;
        }
    }

    public void send(String eventName, BaseMessage msg) {
        if (this.needAck) {
            this.sendWithAck(eventName, msg);
        } else {
            this.sendWithoutAck(eventName, msg);
        }
    }

    //不需要客户端回执
    public void sendWithoutAck(String eventName, BaseMessage msg) {
        this.ioClient.sendEvent(eventName, msg);
    }

    //若需要消息回执，需要使用该方法
    public void sendWithAck(String eventName, BaseMessage msg) {
        final IAck ack = this.ack;
        this.ioClient.sendEvent(eventName, new AckCallback<String>(String.class) {
            @Override
            public void onSuccess(String result) {
                logger.debug("receive ack:{}", result);
                if (ack == null) {
                    logger.error("ack is not initialized");
                    return;
                }

                if (ack instanceof LogAck) {
                    ack.ack(result);
                } else if (ack instanceof RabbitMQAck) {
                    logger.debug("RabbitMQAck is not provided, {}", result);
                } else {
                    logger.debug("No ack is provided, {}", result);
                }
            }
        }, msg);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SocketIOClient getIoClient() {
        return ioClient;
    }

    public void setIoClient(SocketIOClient ioClient) {
        this.ioClient = ioClient;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public IAck getAck() {
        return ack;
    }

    public void setAck(IAck ack) {
        this.ack = ack;
    }

    public String toString() {
        return "{id:" + id + ",uuid:" + uuid.toString() + "}";
    }
}
