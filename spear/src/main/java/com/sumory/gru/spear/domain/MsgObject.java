package com.sumory.gru.spear.domain;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * 传输的消息
 * 
 * <pre>
 * {
 *     type: 1, //1 广播，0 单播给指定target
 *     target: { //单播时该字段有效
 *         id: 10, //"用户id"
 *         type: 1 //1指管理员，0指普通用户，详见com.sumory.gru.spear.domain.UserType
 *     },
 *     content: "字符串"
 * }
 * 
 * </pre>
 * 
 * @author sumory.wu
 * @date 2015年3月18日 下午5:40:50
 */
public class MsgObject {

    private int type;//类型: 1 广播，0 单播给指定target
    private Map<String, Object> target;
    private String content;

    public static MsgType UNICAST = MsgType.UNICAST;
    public static MsgType BRAODCAST = MsgType.BRAODCAST;
    public static MsgType MULTICAST = MsgType.MULTICAST;

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

    public enum MsgType {
        BRAODCAST(1), UNICAST(0), MULTICAST(2);

        private int value;

        MsgType(int v) {
            this.value = v;
        }

        public int getValue() {
            return this.value;
        }

    }

}
