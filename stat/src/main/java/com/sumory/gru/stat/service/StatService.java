package com.sumory.gru.stat.service;

import java.util.List;
import java.util.Set;

import com.sumory.gru.common.domain.StatObject;

public interface StatService {
    public String getServiceVersion();

    /**
     * 获取某个spear节点的群组统计信息：在线的user id列表
     * 
     * @author sumory.wu @date 2015年3月26日 上午11:55:19
     * @param node
     * @param groupId
     * @return
     */
    public Set<String> getGroupStat(long groupId);

    /**
     * 获取某个spear节点的群组统计信息：详见StatObject
     * 
     * @author sumory.wu @date 2015年4月20日 下午12:15:26
     * @param groupId
     * @return
     */
    public List<StatObject> getGroupStatObjectList(long groupId);

    /**
     * 设置某spear节点上group的统计信息
     * 
     * @author sumory.wu @date 2015年3月26日 上午11:56:25
     * @param node 来自的spear节点标识
     * @param groupId 群组id
     * @param seconds 存活时间
     */
    public void setGroupStatOfNode(String node, long groupId, String data, int seconds);

    /**
     * 批量设置某spear节点上group的统计信息，减轻redis压力
     * 
     * @author sumory.wu @date 2015年4月11日 下午1:40:28
     * @param groupStats
     * @param seconds
     */
    public void setGroupStatOfNode(List<String> groupStats, int seconds);

    /**
     * 批量设置某spear节点上group的统计信息，换用bitset，减小传输数据量，减轻redis压力
     * 
     * @author sumory.wu @date 2015年4月20日 上午11:53:43
     * @param node
     * @param stats
     * @param seconds
     */
    public void setGroupStatObjectOfNode(String node, List<StatObject> stats, int seconds);

    /**
     * 保存一个长连接服务节点用户数
     * 
     * @author sumory.wu @date 2015年4月11日 下午1:41:15
     * @param node spear节点标识
     * @param data 此节点在线人数
     * @param seconds 在redis上的存活时间
     */
    public void setUserCountOfNode(String node, String data, int seconds);
}
