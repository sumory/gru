package com.sumory.gru.spear.zk;

import java.io.IOException;

import com.sumory.gru.common.zk.NotifyListener;
import com.sumory.gru.common.zk.ZkClientWatcher;

/**
 * 作为spear节点，需要与zk交互的功能并不多，只要保证节点正常启动时在zk始终保持一个临时节点即可
 * 如需要扩展，可在这里自定义对应事件，ps：注意zk的watch问题
 * 
 * @author sumory.wu
 * @date 2015年3月13日 下午3:04:31
 */
public class SpearZkClientWatcher extends ZkClientWatcher {

    public SpearZkClientWatcher(String zkHost, String baseNode, int sessionTimeout, int retryTimes,
            NotifyListener listener) throws IOException {
        super(zkHost, baseNode, sessionTimeout, retryTimes, listener);
    }

    @Override
    protected void processDataChanged(String path) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void processNodeChildrenChanged(String path) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void processNodeCreated(String path) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void processNodeDeleted(String path) {
        // TODO Auto-generated method stub

    }

}
