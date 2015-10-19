package com.sumory.gru.spear.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 线程池工厂类
 */
public class NamedThreadFactory implements ThreadFactory {

    static final AtomicInteger poolNumber = new AtomicInteger(1);

    final AtomicInteger threadNumber = new AtomicInteger(1);
    final ThreadGroup group;
    final String namePrefix;
    final boolean isDaemon;

    public NamedThreadFactory() {
        this("gru_named_thread_factory");
    }

    public NamedThreadFactory(String name) {
        this(name, false);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    public NamedThreadFactory(String preffix, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
        isDaemon = daemon;
    }

}
