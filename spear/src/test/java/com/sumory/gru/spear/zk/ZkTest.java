package com.sumory.gru.spear.zk;

import java.util.Date;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import com.sumory.gru.common.config.Config;
import com.sumory.gru.common.utils.DateUtil;
import com.sumory.gru.common.zk.ZkClientWatcher;

public class ZkTest {
    public static void main(String[] args) throws Exception {
        String zkHost = Config.get("zk.url");
        String baseNode = Config.get("zk.spear.cluster");
        int sessionTimeout = 3000;
        int retryTimes = 10;
        //创建zookeeper
        ZkClientWatcher zkClientWatcher = new SpearZkClientWatcher(zkHost, baseNode,
                sessionTimeout, retryTimes, null);
        ZooKeeper zk = zkClientWatcher.getZooKeeper();

        zkClientWatcher.createEphemeralNode("/spearnodes/b", "spear-b".getBytes());
        zk.create("/spearnodes/c", "spear-c".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);
        //zk.close();

        System.out.println("__________________________");
        //while (true) {
        System.out.println(DateUtil.toDateTimeString(new Date()));
        List<String> nodes = zk.getChildren(baseNode, true);
        if (nodes != null) {
            for (String s : nodes) {
                System.out.println("子节点：" + baseNode + "/" + s);
                String node = new String(zkClientWatcher.getData(zkClientWatcher, baseNode + "/"
                        + s));
                System.out.println("子节点值：" + node);
            }
        }
        System.out.println("++++++++++++++++++++++++++");
        Thread.sleep(5000);

        //}

        Thread.sleep(Long.MAX_VALUE);
    }
}
