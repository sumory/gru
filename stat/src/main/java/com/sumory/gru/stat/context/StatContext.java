package com.sumory.gru.stat.context;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * stat上下文
 * 
 * @author sumory.wu
 * @date 2015年3月26日 上午11:49:25
 */
public class StatContext {

    private ConcurrentLinkedQueue<String> spearNodes;//当前从zk获取的仍“存活”的spear节点

    private StatContext() {
        spearNodes = new ConcurrentLinkedQueue<String>();
    }

    private static class SingletonHolder {
        final static StatContext instance = new StatContext();
    }

    public static StatContext getInstance() {
        return SingletonHolder.instance;
    }

    public ConcurrentLinkedQueue<String> getSpearNodes() {
        return spearNodes;
    }

    public void setSpearNodes(ConcurrentLinkedQueue<String> spearNodes) {
        this.spearNodes = spearNodes;
    }

}