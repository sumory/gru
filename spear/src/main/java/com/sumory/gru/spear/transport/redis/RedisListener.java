package com.sumory.gru.spear.transport.redis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sumory.gru.common.config.Config;
import com.sumory.gru.common.utils.RedisUtil;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.domain.MsgObject;

/**
 * redis发布订阅监听器
 * 
 * @author sumory.wu
 * @date 2015年10月20日 下午10:23:42
 */
public class RedisListener extends JedisPubSub {
    private final static Logger logger = LoggerFactory.getLogger(RedisListener.class);
    private ExecutorService executor;
    private BlockingQueue<MsgObject> msgQueue;
    private RedisUtil redisUtil;

    private static class SingletonHolder {
        final static RedisListener instance = new RedisListener();
    }

    public static RedisListener getInstance() {
        return SingletonHolder.instance;
    }

    private RedisListener() {
        this.executor = Executors.newFixedThreadPool(1);
        this.msgQueue = SpearContext.getInstance().getMsgQueue();

        JedisPoolConfig config = new JedisPoolConfig();
        //对象池内最大的对象数  
        config.setMaxTotal(Integer.valueOf(Config.get("redis.pool.maxTotal")));
        //最大限制对象数  
        config.setMaxIdle(Integer.valueOf(Config.get("redis.pool.maxIdle")));
        //当池内没有返回对象时，最大等待时间  
        config.setMaxWaitMillis(Long.valueOf(Config.get("redis.pool.maxWaitMillis")));
        config.setTestOnBorrow(Boolean.valueOf(Config.get("redis.pool.testOnBorrow")));
        config.setTestOnReturn(Boolean.valueOf(Config.get("redis.pool.testOnReturn")));
        //通过apache common-pool中的PoolableObjectFactory来管理对象的生成和销户等操作，而ObjectPool来管理对象borrow和return  
        JedisPool jedisPool = new JedisPool(config, Config.get("redis.ip"), Integer.valueOf(Config
                .get("redis.port")));
        redisUtil = new RedisUtil(jedisPool);

    }

    public void publish(final String topic, MsgObject msg) {
        try {
            logger.debug("发送到redis队列topic:{}, 消息:{}", topic, msg);
            redisUtil.pubish(topic, JSONObject.toJSONString(msg));
        }
        catch (Exception e) {
            logger.error("发送消息{}到redis队列{}发生异常", msg, topic, e);
        }
    }

    public void subscribe(final String topic) {
        try {
            logger.info("订阅topic:{}", topic);
            redisUtil.subscribe(this, topic);
        }
        catch (Exception e) {
            logger.error("订阅topic:{}发生异常", topic, e);
        }
    }

    // 取得订阅的消息后的处理  
    @Override
    public void onMessage(String channel, String message) {
        logger.info("从Channel:{} 接收到Msg:{}", channel, message);
        try {
            final MsgObject msg = JSON.parseObject(message, MsgObject.class);

            this.executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        msgQueue.put(msg);//put方法放入一个msg，若queue满了，等到queue有位置
                    }
                    catch (Exception e) {
                        logger.error("往内部消息队列传入消息发生异常", e);
                    }
                }
            });
        }
        catch (Exception e) {
            logger.error("从Channel:{} 接收到Msg:{}, 处理发生异常", channel, message, e);
        }
    }

    // 初始化订阅时候的处理  
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        logger.info(channel + "=" + subscribedChannels);
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
        logger.info(pattern + "=" + channel + "=" + message);
    }

}
