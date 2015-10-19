package com.sumory.gru.common.zk;

import org.apache.zookeeper.ZooKeeper;

public interface NotifyListener {

    public void action(ZooKeeper zookeeper);

}