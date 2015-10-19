package com.sumory.gru.stat.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 用户
 * 
 * @author sumory.wu
 * @date 2015年3月13日 下午3:14:40
 */
public class User {
    private static Logger logger = LoggerFactory.getLogger(User.class);

    private long id;
    private long groupId;
    private String name;

    public User() {
    }

    public User(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
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

}
