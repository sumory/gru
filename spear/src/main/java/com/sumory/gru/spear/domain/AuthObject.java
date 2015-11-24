package com.sumory.gru.spear.domain;

import com.alibaba.fastjson.JSON;

/**
 * 用于鉴权的object
 *
 * @author sumory.wu
 * @date 2015年3月12日 下午7:29:52
 */
public class AuthObject {
    private long id;
    private String name;//名称
    private String appType;//客户端标识
    private String token1;//鉴权用，由业务系统产生
    private String token2;//鉴权用，由ticket产生

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

    public String getToken1() {
        return token1;
    }

    public void setToken1(String token1) {
        this.token1 = token1;
    }

    public String getToken2() {
        return token2;
    }

    public void setToken2(String token2) {
        this.token2 = token2;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
