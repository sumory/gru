package com.sumory.gru.ticket.common.test;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZooKeeperTest {
    public static void main(String[] args) throws Exception {
        Watcher wh = new Watcher() {
            public void process(WatchedEvent event) {
                System.out.println("回调watcher实例： 路径" + event.getPath() + " 类型：" + event.getType());

            }
        };

        ZooKeeper zk = new ZooKeeper("10.60.0.43:2181", 500000, wh);
        System.out.println("---------------------");

        // 创建一个节点root，数据是mydata,不进行ACL权限控制，节点为永久性的(即客户端shutdown了也不会消失)

        Stat s1 = zk.exists("/spearnodes", true);

        if (s1 == null)
            zk.create("/spearnodes", "mydata".getBytes(), Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);

        System.out.println("---------------------");

        // 在root下面创建一个childone znode,数据为childone,不进行ACL权限控制，节点为永久性的

        Stat s = zk.exists("/spearnodes/childone", true);
        if (s != null) {
            zk.delete("/spearnodes/childone", -1);
        }
        zk.create("/spearnodes/childone", "childone".getBytes(), Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        System.out.println("---------------------");

        // 删除/spearnodes/childone这个节点，第二个参数为版本，－1的话直接删除，无视版本

        zk.exists("/spearnodes/childone", true);
        zk.delete("/spearnodes/childone", -1);

        System.out.println("---------------------");

        zk.exists("/spearnodes", true);
        zk.delete("/spearnodes", -1);

        System.out.println("---------------------");

        // 关闭session

        zk.close();
    }
}
