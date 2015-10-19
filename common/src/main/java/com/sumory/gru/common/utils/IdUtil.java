package com.sumory.gru.common.utils;

import com.sumory.gru.common.id.IdWorker;

public class IdUtil {
    private static IdWorker messageIdWorker = new IdWorker(0);
    private static IdWorker clientIdWorker = new IdWorker(1);

    public static long generateMsgId() {
        try {
            return messageIdWorker.nextId();
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long generateClientId() {
        try {
            return clientIdWorker.nextId();
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
