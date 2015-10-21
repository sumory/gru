package com.sumory.gru.spear.domain;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * 传输的消息
 * 
 * <pre>
 * {
 *     type: 1, //1 广播，0 单播
 *     target: { // 给指定target
 *         id: 10, //单播时指目标用户id，广播时指群组id
 *         type: 1 //扩展字段，暂时无用
 *     },
 *     content: "字符串"//消息内容
 * }
 * 
 * </pre>
 * 
 * @author sumory.wu
 * @date 2015年3月18日 下午5:40:50
 */
public class MsgObject {

    private long fromId;//发送者id
    private int type;//类型: 1 广播，0 单播给指定target
    private Map<String, Object> target;
    private String content;

    public static MsgType UNICAST = MsgType.UNICAST;
    public static MsgType BRAODCAST = MsgType.BRAODCAST;
    public static MsgType MULTICAST = MsgType.MULTICAST;

    public long getFromId() {
        return fromId;
    }

    public void setFromId(long fromId) {
        this.fromId = fromId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map<String, Object> getTarget() {
        return target;
    }

    public void setTarget(Map<String, Object> target) {
        this.target = target;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static void main(String[] args) {
        MsgObject o = new MsgObject();
        Map<String, Object> target = new HashMap<String, Object>();
        target.put("id", 10);
        target.put("type", 1);
        o.setType(0);
        o.setContent("send");
        o.setTarget(target);

        System.out.println(o);

        System.out.println(MsgObject.BRAODCAST);
    }

}
