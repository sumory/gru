package com.sumory.gru.spear.thread;

import java.util.concurrent.ThreadFactory;

/**
 * 
 */
public class NameThreadFactory implements ThreadFactory {
    private String nameSample = "Thread-%s";
    private int index = 1;

    public NameThreadFactory(String nameSample) {
        this.nameSample = nameSample;
    }

    public Thread newThread(Runnable run) {
        Thread t = new Thread(run);
        t.setName(String.format(nameSample, index++));
        t.setDaemon(true);
        return t;
    }
}