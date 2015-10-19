package com.sumory.gru.spear.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class ExecutesManager {
    private ThreadPoolExecutor defaultExecutor = null;
    private final Map<String, ThreadPoolExecutor> servicePoolCache = new HashMap<String, ThreadPoolExecutor>();

    //
    public ExecutesManager(int minCorePoolSize, int maxCorePoolSize, int queueSize,
            long keepAliveTime) {
        final BlockingQueue<Runnable> inWorkQueue = new LinkedBlockingQueue<Runnable>(queueSize);
        this.defaultExecutor = new ThreadPoolExecutor(minCorePoolSize, maxCorePoolSize,//
                keepAliveTime, TimeUnit.SECONDS, inWorkQueue,//
                new NameThreadFactory("GRU-SPEAR-%s"), new ThreadPoolExecutor.AbortPolicy());
    }

    //
    public Executor getExecute(String serviceUniqueName) {
        if (this.servicePoolCache.isEmpty() == false) {
            ThreadPoolExecutor executor = this.servicePoolCache.get(serviceUniqueName);
            if (executor != null) {
                return executor;
            }
        }
        return this.defaultExecutor;
    }
}