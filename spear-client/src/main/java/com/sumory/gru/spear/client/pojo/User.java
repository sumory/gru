package com.sumory.gru.spear.client.pojo;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long classId;
    private int type;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
