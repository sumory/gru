package com.sumory.gru.spear.zk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.common.zk.ZkNode;

public class SpearNotifyListener implements NotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(SpearNotifyListener.class);
    private List<ZkNode> zkNodes;

    public SpearNotifyListener(ZkNode... zkNodes) {
        if (zkNodes != null && zkNodes.length >= 1)
            this.zkNodes = Arrays.asList(zkNodes);
        else
            this.zkNodes = new ArrayList<ZkNode>();
    }

    @Deprecated
    private void addZkNode(ZkNode node) {
        this.zkNodes.add(node);
    }

    private boolean checkExists(ZooKeeper zk, String node) {
        try {
            Stat s = zk.exists(node, true);
            boolean exists = (s != null ? true : false);
            if (exists)
                logger.debug("{} 存在", node);
            else
                logger.debug("{} 不存在", node);

            return exists;
        }
        catch (Exception e) {
            logger.warn("checkExists异常 " + node, e);
            return false;
        }
    }

    /**
     * 在zk首次连接，或者其他情况导致重连（这个时候EPHEMERAL节点已消失）后，重新执行该操作
     */
    @Override
    public void action(ZooKeeper zk) {
        synchronized (zk) {
            for (ZkNode n : zkNodes) {
                try {
                    logger.info("检查节点：{}", n.getPath());
                    if (!checkExists(zk, n.getPath())) {
                        logger.info("节点：{} 不存在，尝试创建{}", n.getPath(), n.toString());
                        zk.create(n.getPath(), n.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                n.getCreateMode());
                        logger.info("尝试创建后检查节点{} 是否存在：{}", n.getPath(),
                                checkExists(zk, n.getPath()));
                    }
                }
                catch (Exception e) {
                    logger.error("检查节点，不存在则创建，过程中出错", e);
                }
            }
        }
    }
}
