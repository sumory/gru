package com.sumory.gru.spear.message;

import com.alibaba.fastjson.JSONObject;

// 字符串消息
public class StringMessage extends BaseMessage {
    private String content;

    public StringMessage(long id, String content) {
        super(id);
        this.createTime = System.currentTimeMillis();
        this.content = content;
    }

    public String getContent() {
        return content;

    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
