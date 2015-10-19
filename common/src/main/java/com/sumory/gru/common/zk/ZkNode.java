package com.sumory.gru.common.zk;

import org.apache.zookeeper.CreateMode;

public class ZkNode {
    private String path;
    private byte[] data;
    private CreateMode createMode;

    public ZkNode(String path, byte[] data, CreateMode createMode) {
        this.path = path;
        this.data = data;
        this.createMode = createMode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public CreateMode getCreateMode() {
        return createMode;
    }

    public void setCreateMode(CreateMode createMode) {
        this.createMode = createMode;
    }

    public String toString() {
        return String.format("{path:%s, data:%s, createMode:%s}", path, new String(data),
                createMode.toString());
    }
}
