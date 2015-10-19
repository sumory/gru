package com.sumory.gru.stat.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sumory.gru.common.domain.StatObject;
import com.sumory.gru.stat.context.StatContext;
import com.sumory.gru.stat.redis.RedisStore;
import com.sumory.gru.stat.service.StatService;

@Service(value = "statService")
public class StatServiceImpl implements StatService {
    private static final Logger logger = LoggerFactory.getLogger(StatServiceImpl.class);

    @Value("${dubbo.gru.stat.version}")
    private String version;

    @Override
    public String getServiceVersion() {
        return version;
    }

    @Override
    public Set<String> getGroupStat(long groupId) {
        StatContext context = StatContext.getInstance();
        ConcurrentLinkedQueue<String> spearNodes = context.getSpearNodes();
        Set<String> userIdSet = new HashSet<String>();
        if (!spearNodes.isEmpty()) {
            Iterator<String> it = spearNodes.iterator();
            while (it.hasNext()) {
                String node = it.next();
                String userIdOfNode = RedisStore.getInstance().getGroupStatOfNode(node, groupId);//从redis获取用户列表
                if (!StringUtils.isBlank(userIdOfNode)) {
                    String[] users = StringUtils.split(userIdOfNode, ",");
                    if (users != null) {
                        userIdSet.addAll(Arrays.asList(users));
                    }
                }
            }

        }
        return userIdSet;
    }

    @Override
    public List<StatObject> getGroupStatObjectList(long groupId) {
        StatContext context = StatContext.getInstance();
        ConcurrentLinkedQueue<String> spearNodes = context.getSpearNodes();
        List<StatObject> stats = new ArrayList<>();

        if (!spearNodes.isEmpty()) {
            Iterator<String> it = spearNodes.iterator();
            while (it.hasNext()) {
                String node = it.next();
                StatObject statObject = RedisStore.getInstance().getGroupStatObjectOfNode(node,
                        groupId);//从redis获取用户列表

                if (statObject != null)
                    stats.add(statObject);
            }

        }
        return stats;
    }

    @Override
    public void setGroupStatOfNode(String node, long groupId, String data, int seconds) {
        try {
            RedisStore.getInstance().setGroupStatOfNode(node, groupId, data, seconds);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("保存群组统计1异常", e);
        }
    }

    @Override
    public void setGroupStatOfNode(List<String> groupStats, int seconds) {
        for (String s : groupStats) {
            try {
                String[] stats = s.split("_");
                RedisStore.getInstance().setGroupStatOfNode(stats[0], new Long(stats[1]), stats[2],
                        seconds);
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.error("保存群组统计2异常", e);
            }

        }
    }

    @Override
    public void setGroupStatObjectOfNode(String node, List<StatObject> stats, int seconds) {
        for (StatObject s : stats) {
            try {
                RedisStore.getInstance().setGroupStatOfNode(node, s, seconds);
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.error("保存群组统计3异常", e);
            }

        }
    }

    @Override
    public void setUserCountOfNode(String node, String data, int seconds) {
        RedisStore.getInstance().setUserCountOfNode(node, data, seconds);
    }
}
