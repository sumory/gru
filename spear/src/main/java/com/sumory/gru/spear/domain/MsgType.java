package com.sumory.gru.spear.domain;

public enum MsgType {
    BRAODCAST(1), UNICAST(0), MULTICAST(2);

    private int value;

    MsgType(int v) {
        this.value = v;
    }

    public int getValue() {
        return this.value;
    }

}