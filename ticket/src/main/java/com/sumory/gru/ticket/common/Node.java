package com.sumory.gru.ticket.common;

/**
 * 节点基类
 * 
 * @author sumory.wu
 * @date 2015年3月12日 下午2:44:13
 */
public class Node {
    String name;
    String addr;//需唯一

    public Node(String name, String addr) {
        this.name = name;
        this.addr = addr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    @Override
    public String toString() {
        return this.name + "#" + this.addr;
    }
}