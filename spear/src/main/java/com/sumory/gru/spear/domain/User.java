package com.sumory.gru.spear.domain;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.corundumstudio.socketio.SocketIOClient;
import com.sumory.gru.spear.message.BaseMessage;

/**
 * 用户：可能属于多个群组，包含一坨client
 * 
 * @author sumory.wu
 * @date 2015年10月18日 下午2:35:59
 */
public class User {
    private final static Logger logger = LoggerFactory.getLogger(User.class);

    private long id;
    private List<Group> groups;//一个人可能属于多个组
    private String name;

    @JSONField(serialize = false)
    private ConcurrentLinkedQueue<Client> clients;//一个用户可能有几个client同时登录

    public User() {
        this.clients = new ConcurrentLinkedQueue<Client>();
    }

    public User(long id) {
        this.id = id;
        this.clients = new ConcurrentLinkedQueue<Client>();
    }

    public ConcurrentLinkedQueue<Client> getClients() {
        return clients;
    }

    public void setClients(ConcurrentLinkedQueue<Client> clients) {
        this.clients = clients;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public String stats() {
        return "user id:" + id + " user clients count:" + clients.size();
    }

    public void send(String eventName, BaseMessage msg) {
        logger.debug("send msg to user-> thread:{} userId:{} clients size:{} eventName:{} msg:{}",
                Thread.currentThread().getName(), id, this.clients.size(), eventName, msg);
        for (Client client : clients) {
            client.send(eventName, msg);
        }
        logger.debug("send msg to user finished");
    }

    public void dismiss() {
        logger.debug("user dismiss all clients, thread:{} userId:{} clientsSize:{}", Thread
                .currentThread().getName(), id, this.clients.size());

        for (Client client : clients) {
            try {
                client.ioClient.disconnect();
            }
            catch (Exception e) {
            }
        }

        logger.debug("user dismiss all clients finished");
    }

    public void addClientToUser(Client client) {
        if (client != null) {
            logger.debug("addClientToUser, userId:{} userName:{} clientId:{}", id, name,
                    client.getId());
            this.clients.add(client);
        }
    }

    public void removeClientFromUser(Client client) {
        if (client != null) {
            logger.debug("removeClientFromUser, userId:{} userName:{} clientId:{}", id, name,
                    client.getId());

            Iterator<Client> iterator = this.clients.iterator();
            while (iterator.hasNext()) {
                Client c = iterator.next();
                if (client == c || client.getUuid() == c.getUuid() || client.getId() == c.getId()) {
                    this.clients.remove(c);
                    logger.debug("removeClientFromUser done, userId:{} userName:{} clientId:{}",
                            id, name, client.getId());
                }
            }
        }
    }

    public void removeClientFromUser(SocketIOClient ioClient) {
        if (ioClient != null) {
            logger.debug("removeClientFromUser, userId:{} userName:{} socketIOClientId:{}", id,
                    name, ioClient.getSessionId());

            Iterator<Client> iterator = this.clients.iterator();
            while (iterator.hasNext()) {
                Client c = iterator.next();
                SocketIOClient ioc = c.getIoClient();
                if (ioc != null
                        && (ioClient == ioc || ioClient.getSessionId() == ioc.getSessionId())) {
                    this.clients.remove(c);
                    logger.debug(
                            "removeClientFromUser done, userId:{} userName:{} clientId:{} socketIOClientId:{}",
                            id, name, c.getId(), ioClient.getSessionId());
                }
            }
        }
    }
}
