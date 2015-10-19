package com.sumory.gru.stat.redis;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.sumory.gru.common.config.Config;
import com.sumory.gru.common.domain.StatObject;
import com.sumory.gru.common.utils.RedisUtil;

/**
 * redis存储
 * 
 * @author sumory.wu
 * @date 2015年3月9日 下午3:43:56
 */
public class RedisStore {

    private RedisUtil redisUtil;
    private String userCountPrefix = "user_count:";//某节点用户数在线统计
    private String groupStatPrefix = "gru_stat:";//群组在线统计

    private static class SingletonHolder {
        final static RedisStore instance = new RedisStore();
    }

    public static RedisStore getInstance() {
        return SingletonHolder.instance;
    }

    private RedisStore() {
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

    /**
     * 获取某个spear节点的群组统计信息：在线的user id列表
     * 
     * @author sumory.wu @date 2015年3月26日 上午11:55:19
     * @param node
     * @param groupId
     * @return
     */
    public String getGroupStatOfNode(String node, long groupId) {
        if (StringUtils.isBlank(node))
            return null;
        return redisUtil.getString(buildGroupStatKey(node, groupId));
    }

    /**
     * 获取某个spear节点的群组统计信息：详见StatObject类
     * 
     * @author sumory.wu @date 2015年4月20日 下午12:14:14
     * @param node
     * @param groupId
     * @return
     */
    public StatObject getGroupStatObjectOfNode(String node, long groupId) {
        if (StringUtils.isBlank(node))
            return null;
        return (StatObject) redisUtil.get(buildGroupStatKey(node, groupId));
    }

    /**
     * 设置某spear节点上group的统计信息
     * 
     * @author sumory.wu @date 2015年3月26日 上午11:56:25
     * @param node 来自的spear节点标识
     * @param groupId 群组id
     * @param seconds 存活时间
     * @return
     */
    public boolean setGroupStatOfNode(String node, long groupId, String data, int seconds) {
        if (StringUtils.isBlank(node) || StringUtils.isBlank(data) || seconds <= 0)
            return false;
        String key = buildGroupStatKey(node, groupId);
        redisUtil.setString(key, data);
        redisUtil.expire(key, seconds);
        return true;
    }

    /**
     * 设置某spear节点上group的统计信息
     * 
     * @author sumory.wu @date 2015年4月20日 上午11:45:36
     * @param node 节点标识
     * @param stat 统计对象
     * @param seconds 过期时间
     * @return
     */
    public boolean setGroupStatOfNode(String node, StatObject stat, int seconds) {
        if (StringUtils.isBlank(node) || stat == null || seconds <= 0)
            return false;
        String key = buildGroupStatKey(node, stat.getGroupId());
        redisUtil.set(key, stat);
        redisUtil.expire(key, seconds);
        return true;
    }

    /**
     * 保存某个spear节点的用户数
     * 
     * @author sumory.wu @date 2015年4月11日 下午1:48:51
     * @param node
     * @param data
     * @param seconds
     * @return
     */
    public boolean setUserCountOfNode(String node, String data, int seconds) {
        if (StringUtils.isBlank(node) || StringUtils.isBlank(data) || seconds <= 0)
            return false;
        String key = buildUserCountKey(node);
        redisUtil.setString(key, data);
        redisUtil.expire(key, seconds);
        return true;
    }

    private String buildGroupStatKey(String node, long groupId) {
        return groupStatPrefix + node + "_" + groupId;
    }

    private String buildUserCountKey(String node) {
        return userCountPrefix + node;
    }
}
