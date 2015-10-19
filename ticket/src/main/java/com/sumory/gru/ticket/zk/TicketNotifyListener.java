package com.sumory.gru.ticket.zk;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.ticket.common.Node;
import com.sumory.gru.ticket.common.Shard;

/**
 * 监听zk变化，获取或更新本地spear节点
 * 
 * @author sumory.wu
 * @date 2015年3月11日 下午4:09:37
 */
public class TicketNotifyListener implements NotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(TicketNotifyListener.class);

    private Shard<Node> shard;
    private String baseNode;//spear cluster的根目录

    public TicketNotifyListener(String baseNode, Shard<Node> shard) {
        this.shard = shard;
        this.baseNode = baseNode;
    }

    /**
     * 
     */
    @Override
    public void action(ZooKeeper zk) {
        logger.info("监听器action，需重新执行初始化shard");

        //            Stat s1 = zk.exists(this.baseNode, true);

        //            if (s1 == null)
        //                zk.create("/spearnodes", "mydata".getBytes(), Ids.OPEN_ACL_UNSAFE,
        //                        CreateMode.PERSISTENT);
        //            zk.exists(this.baseNode, true);
        //ZkUtil.getAllSpearNodes(this.baseNode, zk);
        List<String> zkNodes = null;

        try {
            zkNodes = ZkUtil.getAllSpearNodes(this.getBaseNode(), zk);
            if (zkNodes != null) {//nodes节点不为空
                List<Node> newNodes = new ArrayList<Node>();
                for (String zkNode : zkNodes) {
                    String childNode = this.getBaseNode() + "/" + zkNode;
                    logger.info("获取子节点数据：" + childNode);
                    String childNodeData = new String(zk.getData(childNode, true, null));
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

    public Shard<Node> getShard() {
        return shard;
    }

    public void setShard(Shard<Node> shard) {
        this.shard = shard;
    }

    public String getBaseNode() {
        return baseNode;
    }

    public void setBaseNode(String baseNode) {
        this.baseNode = baseNode;
    }

}
