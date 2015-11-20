package com.sumory.gru.spear.domain;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * 用户订阅群组的object
 *
 * @author sumory.wu
 * @date 2015年10月18日 下午3:36:00
 */
public class SubscribeObject {
    private long userId;
    private List<SubscribeGroup> subscribeGroups;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<SubscribeGroup> getSubscribeGroups() {
        return subscribeGroups;
    }

    public void setSubscribeGroups(List<SubscribeGroup> subscribeGroups) {
        this.subscribeGroups = subscribeGroups;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public class SubscribeGroup {
        private long id;
        private String name;
        private Map<String, Object> extra;

        public SubscribeGroup() {
        }

        public SubscribeGroup(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }
    }

}
