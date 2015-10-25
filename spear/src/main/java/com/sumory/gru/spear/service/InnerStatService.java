package com.sumory.gru.spear.service;

import java.util.List;
import java.util.Set;

import com.sumory.gru.common.domain.StatObject;
import com.sumory.gru.stat.service.StatService;

/**
 * 由于最简化部署时不提供stat功能，所以这里没有实现stat服务
 * 
 * @author sumory.wu
 * @date 2015年10月25日 下午8:37:35
 */
public class InnerStatService implements StatService {

    @Override
    public String getServiceVersion() {
        return "inner";
    }

    @Override
    public Set<String> getGroupStat(long groupId) {
        return null;
    }

    @Override
    public List<StatObject> getGroupStatObjectList(long groupId) {
        return null;
    }

    @Override
    public void setGroupStatOfNode(String node, long groupId, String data, int seconds) {

    }

    @Override
    public void setGroupStatOfNode(List<String> groupStats, int seconds) {

    }

    @Override
    public void setGroupStatObjectOfNode(String node, List<StatObject> stats, int seconds) {

    }

    @Override
    public void setUserCountOfNode(String node, String data, int seconds) {

    }

}
