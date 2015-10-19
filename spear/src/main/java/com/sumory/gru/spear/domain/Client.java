package com.sumory.gru.spear.domain;

import java.util.UUID;

import com.corundumstudio.socketio.SocketIOClient;
import com.sumory.gru.spear.message.BaseMessage;

/**
 * 一个client对应一个连接实体，比如一个手机端连接，一个chrome tab页里的连接等
 * 
 * @author sumory.wu
 * @date 2015年3月13日 下午3:13:46
 */
public class Client {

    public long id;
    public UUID uuid;
    public transient SocketIOClient ioClient;

    public Client() {

    }

    public Client(long id, SocketIOClient ioClient) {
        this.ioClient = ioClient;
        this.uuid = this.ioClient.getSessionId();
        this.id = id;
    }

    public void send(String eventName, BaseMessage msg) {
        this.ioClient.sendEvent(eventName, msg);
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

    public String toString() {
        return "{id:" + id + ",uuid:" + uuid.toString() + "}";
    }
}
