package com.sumory.gru.stat.zk;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.stat.context.StatContext;

public class StatNotifyListener implements NotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(StatNotifyListener.class);

    private StatContext context;
    private String baseNode;//spear cluster的根目录

    public StatNotifyListener(String baseNode, StatContext context) {
        this.baseNode = baseNode;
        this.context = context;
    }

    /**
     * 
     */
    @Override
    public void action(ZooKeeper zk) {
        logger.info("监听器action，需重新执行初始化spear存活列表");
        List<String> zkNodes = null;

        try {
            zkNodes = ZkUtil.getAllSpearNodes(this.getBaseNode(), zk);
            if (zkNodes != null && zkNodes.size() > 0) {//nodes节点不为空
                ConcurrentLinkedQueue<String> spearNodes = this.context.getSpearNodes();
                spearNodes.clear();
                for (String zkNode : zkNodes) {
                    logger.info("获得的spear节点：{}，将其加入到存活列表中", zkNode);
                    if (!StringUtils.isBlank(zkNode))
                        spearNodes.add(zkNode);
                }
            }
        }
        catch (KeeperException e) {
            if (e instanceof NoNodeException) {//节点下无子节点
                logger.warn(this.getBaseNode() + "下无子节点", e);
            }
        }
        catch (InterruptedException e) {
            logger.error("reset nodes发生异常", e);
        }
        catch (Exception e) {
            logger.error("reset nodes发生异常", e);
        }

    }

    public String getBaseNode() {
        return baseNode;
    }

    public void setBaseNode(String baseNode) {
        this.baseNode = baseNode;
    }

}
