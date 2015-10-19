package com.sumory.gru.spear.message;


/**
 * 基础消息类，可自行扩展<br/>
 * 后期考虑尽量减小消息体长度
 * 
 * @author sumory.wu
 * @date 2015年2月4日 下午6:41:26
 */
public abstract class BaseMessage {
    public long id;
    public long createTime;//产生时间

    public transient long expireTime;//过期时间（绝对时间，非间隔）

    public BaseMessage(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

}
