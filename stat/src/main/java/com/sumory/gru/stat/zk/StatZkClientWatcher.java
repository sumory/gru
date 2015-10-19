package com.sumory.gru.stat.zk;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.common.zk.ZkClientWatcher;
import com.sumory.gru.stat.context.StatContext;

public class StatZkClientWatcher extends ZkClientWatcher {
    private static final Logger logger = LoggerFactory.getLogger(StatZkClientWatcher.class);
    private StatContext context;

    public StatZkClientWatcher(String zkHost, String baseNode, int sessionTimeout, int retryTimes,
            NotifyListener listener, StatContext context) throws IOException {
        super(zkHost, baseNode, sessionTimeout, retryTimes, listener);
        this.context = context;
    }

    @Override
    protected void processDataChanged(String path) {
        logger.info(path + " data changed");
        resetStatNodes();
        reWatch();
    }

    @Override
    protected void processNodeChildrenChanged(String path) {
        logger.info(path + " children changed");
        resetStatNodes();
        reWatch();
    }

    @Override
    protected void processNodeCreated(String path) {
        logger.info(path + " node created");
        resetStatNodes();
        reWatch();
    }

    @Override
    protected void processNodeDeleted(String path) {
        logger.info(path + " node deleted");
        resetStatNodes();
        reWatch();
    }

    /**
     * 从zk上拿到最新的节点，重置本地缓存节点
     * 
     * @author sumory.wu @date 2015年3月26日 上午11:47:41
     */
    private void resetStatNodes() {
        List<String> zkNodes = null;
        logger.info("重置存活列表");
        try {
            zkNodes = ZkUtil.getAllSpearNodes(this.getBaseNode(), this.getZooKeeper());
            if (zkNodes != null && zkNodes.size() > 0) {//nodes节点不为空
                ConcurrentLinkedQueue<String> spearNodes = this.context.getSpearNodes();
                spearNodes.clear();
                for (String zkNode : zkNodes) {
                    logger.info("获得的spear节点：{}，将其加入到存活列表中", zkNode);
                    if (!StringUtils.isBlank(zkNode))
                        spearNodes.add(zkNode);
                }
            }
            else {
                logger.debug("{}下无子节点", this.getBaseNode());
            }
        }
        catch (KeeperException e) {
            if (e instanceof NoNodeException) {//节点下无子节点
                logger.warn(this.getBaseNode() + "下无子节点", e);
            }
        }
        catch (InterruptedException e) {
            logger.error("reset stat nodes发生异常", e);
        }
        catch (Exception e) {
            logger.error("reset stat nodes发生异常", e);
        }
    }

    //再次监听
    private void reWatch() {
        try {
            ZkUtil.watchNodes(this.getBaseNode(), this.getZooKeeper());
            //  ZkUtil.getAllSpearNodes("/spearnodes", this.getZooKeeper());
        }
        catch (Exception e) {
            logger.error("reWatch nodes发生异常", e);
        }
    }

}
