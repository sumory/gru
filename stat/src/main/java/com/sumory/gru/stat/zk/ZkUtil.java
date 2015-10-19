package com.sumory.gru.stat.zk;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.common.zk.ZkClientWatcher;
import com.sumory.gru.stat.context.StatContext;

public class ZkUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZkUtil.class);

    public static List<String> getAllSpearNodes(String baseNode, ZooKeeper zk)
            throws KeeperException, InterruptedException, IOException {
        List<String> nodes = zk.getChildren(baseNode, true);
        return nodes;
    }

    public static void watchNodes(String baseNode, ZooKeeper zk) throws KeeperException,
            InterruptedException, IOException {
        List<String> nodes = zk.getChildren(baseNode, true);
        System.out.println("watch根节点：" + baseNode);
        zk.exists(baseNode, true);
        if (nodes != null) {
            for (String s : nodes) {
                System.out.println("watch子节点：" + baseNode + "/" + s);
                zk.exists(baseNode + "/" + s, true);
            }
        }
    }

    public static void initListener(String zkHost, String baseNode, int sessionTimeout,
            int retryTimes, StatContext context) throws Exception {
        //建立zk Node事件监听器
        NotifyListener listener = new StatNotifyListener(baseNode, context);
        //创建zookeeper
        ZkClientWatcher zkClientWatcher = new StatZkClientWatcher(zkHost, baseNode, sessionTimeout,
                retryTimes, listener, context);
        ZooKeeper zk = zkClientWatcher.getZooKeeper();

        //判断根节点是否存在，不存在则建一个
        Stat spearRootNodeStat = zk.exists(baseNode, true);
        if (spearRootNodeStat == null) {
            zk.create(baseNode, "created by stat server".getBytes(), Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }

        //初始化时打印日志
        logger.info("查看stat server zk启动时已经存在的spear nodes:");
        List<String> ns = getAllSpearNodes(baseNode, zk);
        if (ns != null) {
            for (String n : ns) {
                logger.info(n);
            }
        }
    }
}
