package com.sumory.gru.spear.zk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.common.zk.ZkClientWatcher;
import com.sumory.gru.common.zk.ZkNode;

public class ZkUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZkUtil.class);

    public static void getAllSpearNodes(String baseNode, ZkClientWatcher zkClientWatcher)
            throws KeeperException, InterruptedException, IOException {
        List<String> nodes = zkClientWatcher.getZooKeeper().getChildren(baseNode, false);
        if (nodes != null) {
            for (String s : nodes) {
                System.out.println("子节点：" + baseNode + "/" + s);
                String node = new String(zkClientWatcher.getData(zkClientWatcher, baseNode + "/"
                        + s));
                System.out.println("子节点值：" + node);
            }
        }
    }

    public static void initNode(String zkHost, String baseNode, String outAddr, String spearId,
            int sessionTimeout, int retryTimes) throws Exception {

        //初始化listener
        String spearNode = baseNode + "/spear" + spearId;
        List<ZkNode> zkNodes = new ArrayList<ZkNode>();
        zkNodes.add(new ZkNode(baseNode, "spear cluster nodes".getBytes(), CreateMode.PERSISTENT));

        String spearNodeData = ("spear" + spearId) + "#http://" + outAddr;//例如：spear8#10.60.0.43:31001，严格遵守该格式，省去序列化麻烦
        zkNodes.add(new ZkNode(spearNode, spearNodeData.getBytes(), CreateMode.EPHEMERAL));
        NotifyListener listener = new SpearNotifyListener(
                zkNodes.toArray(new ZkNode[zkNodes.size()]));

        //创建zookeeper
        ZkClientWatcher zkClientWatcher = new SpearZkClientWatcher(zkHost, baseNode,
                sessionTimeout, retryTimes, listener);

        getAllSpearNodes(baseNode, zkClientWatcher);
    }

}
