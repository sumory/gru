package com.sumory.gru.spear.domain;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sumory.gru.spear.message.BaseMessage;

/**
 * 群组：用于包含一坨用户
 * 
 * @author sumory.wu
 * @date 2015年3月13日 下午3:14:25
 */
public class Group {
    private static Logger logger = LoggerFactory.getLogger(Group.class);

    private String name;
    private long id;
    private String uniqId;//业务上用作唯一标识
    private ConcurrentLinkedQueue<User> users;

    public Group() {
        this.users = new ConcurrentLinkedQueue<User>();
    }

    public Group(long id) {
        this.id = id;
        this.users = new ConcurrentLinkedQueue<User>();
    }

    public int getUserCount() {
        return this.users.size();
    }

    /** broadcast会被多个consumer线程调用 */
    public void broadcast(String eventName, BaseMessage msg) {
        logger.debug("群组:{}broadcast, t_name:{} user_count:{}", id, Thread.currentThread()
                .getName(), users.size());//粗略观察有20个线程,后续已改为自定义线程池发送
        try {
            Iterator<User> iterator = users.iterator();
            while (iterator.hasNext()) {
                User u = iterator.next();
                u.send(eventName, msg);
            }
        }
        catch (Exception e) {
            logger.error("群发异常, groupId:{}", id, e);
        }
    }

    /** 解散群组，断开群组下的所有连接 */
    public synchronized void dismiss() {
        logger.debug("群组" + id + " dismiss, thread name:" + Thread.currentThread().getName());
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User u = iterator.next();
            u.dismiss();
        }
    }

    /** 替换group里的某个user */
    public void replaceUserOfGroup(User newUser) {
        if (newUser != null) {
            logger.debug("replaceUserOfGroup, new userId:" + newUser.getId());
            Iterator<User> iterator = users.iterator();
            while (iterator.hasNext()) {
                User u = iterator.next();
                if (newUser.getId() == u.getId()) {
                    users.remove(u);
                    users.add(newUser);
                    logger.debug("replaceUserOfGroup done, new userId:{}", newUser.getId());
                }
            }
        }
    }

    public String stats() {
        return "groupId:" + id + " groupUserCount:" + users.size();
    }

    /** 往群组的用户列表里添加用户 */
    public void addUserToGroup(User user) {
        if (user != null) {
            logger.debug("addUserToGroup, groupId:{} userId:{}", id, user.getId());
            this.users.add(user);
        }
    }

    /** 从群组用户列表里删除用户 */
    public void removeUserFromGroup(User user) {
        logger.debug("removeUserFromGroup,  groupId:{} userId:{}", id, user.getId());

        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User u = iterator.next();
            if (user.getId() == u.getId() || user == u) {//若user的id相同也需要删除
                users.remove(u);
                logger.debug("removeUserFromGroup done, groupId:{} userId:{}", id, user.getId());
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUniqId() {
        return uniqId;
    }

    public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
    }

    public ConcurrentLinkedQueue<User> getUsers() {
        return users;
    }

    public void setUsers(ConcurrentLinkedQueue<User> users) {
        this.users = users;
    }

    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
