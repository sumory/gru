package com.sumory.gru.spear.transport.redis;

import redis.clients.jedis.JedisPubSub;

public class RedisListener extends JedisPubSub {

    // 取得订阅的消息后的处理  
    @Override
    public void onMessage(String channel, String message) {
        System.out.println(channel + "=" + message);
    }

    // 初始化订阅时候的处理  
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        // System.out.println(channel + "=" + subscribedChannels);  
    }

    // 取消订阅时候的处理
    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        // System.out.println(channel + "=" + subscribedChannels);  
    }

    // 初始化按表达式的方式订阅时候的处理 
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        // System.out.println(pattern + "=" + subscribedChannels);  
    }

    // 取消按表达式的方式订阅时候的处理  
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        // System.out.println(pattern + "=" + subscribedChannels);  
    }

    // 取得按表达式的方式订阅的消息后的处理 
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println(pattern + "=" + channel + "=" + message);
    }

}
