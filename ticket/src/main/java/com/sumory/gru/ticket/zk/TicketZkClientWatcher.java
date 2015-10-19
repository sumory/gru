package com.sumory.gru.ticket.zk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.common.zk.ZkClientWatcher;
import com.sumory.gru.ticket.common.Node;
import com.sumory.gru.ticket.common.Shard;

public class TicketZkClientWatcher extends ZkClientWatcher {
    private static final Logger logger = LoggerFactory.getLogger(TicketZkClientWatcher.class);

    private Shard<Node> shard;

    public TicketZkClientWatcher(String zkHost, String baseNode, int sessionTimeout,
            int retryTimes, NotifyListener listener, Shard<Node> shard) throws IOException {
        super(zkHost, baseNode, sessionTimeout, retryTimes, listener);
        this.shard = shard;
    }

    @Override
    protected void processDataChanged(String path) {
        logger.info(path + " data changed");
        resetShardNodes();
        reWatch();
    }

    @Override
    protected void processNodeChildrenChanged(String path) {
        logger.info(path + " children changed");
        resetShardNodes();
        reWatch();
    }

    @Override
    protected void processNodeCreated(String path) {
        logger.info(path + " node created");
        resetShardNodes();
        reWatch();
    }

    @Override
    protected void processNodeDeleted(String path) {
        logger.info(path + " node deleted");
        resetShardNodes();
        reWatch();
    }

    /**
     * 重置用于一致性hash的节点<br/>
     * 从zk上拿到最新的节点，从shard里删除已经不存在的，增加新增的
     * 
     * @author sumory.wu @date 2015年3月12日 上午9:57:08
     */
    private void resetShardNodes() {
        List<String> zkNodes = null;

        try {
            zkNodes = ZkUtil.getAllSpearNodes(this.getBaseNode(), this.getZooKeeper());
            if (zkNodes != null) {//nodes节点不为空
                List<Node> newNodes = new ArrayList<Node>();
                for (String zkNode : zkNodes) {
                    String childNode = this.getBaseNode() + "/" + zkNode;
                    logger.info("获取子节点数据：" + childNode);
                    String childNodeData = new String(this.getZooKeeper().getData(childNode, true,
                            null));
                    logger.info("子节点数据：" + childNodeData);
                    String[] parts = childNodeData.split("#");
                    Node n = new Node(parts[0], parts[1]);
                    newNodes.add(n);
                }
                shard.resetNodes(newNodes);
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

    //再次监听
    private void reWatch() {
        try {
            ZkUtil.watchNodes(this.getBaseNode(), this.getZooKeeper());
            //  ZkUtil.getAllSpearNodes("/spearnodes", this.getZooKeeper());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
