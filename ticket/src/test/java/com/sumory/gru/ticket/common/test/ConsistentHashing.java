package com.sumory.gru.ticket.common.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashing { // S类封装了机器节点的信息 ，如name、password、ip、port等

    static private TreeMap<Long, Node> nodes; // 虚拟节点到真实节点的映射
    static private TreeMap<Long, Node> treeKey; //key到真实节点的映射
    static private List<Node> shards = new ArrayList<Node>(); // 真实机器节点
    private final int NODE_NUM = 100; // 每个机器节点关联的虚拟节点个数
    boolean flag = false;

    public ConsistentHashing(List<Node> shards) {
        super();
        this.shards = shards;
        init();
    }

    public static void main(String[] args) {
        //      System.out.println(hash("w222o1d"));
        //      System.out.println(Long.MIN_VALUE);
        //      System.out.println(Long.MAX_VALUE);
        Node s1 = new Node("s1", "192.168.1.1");
        Node s2 = new Node("s2", "192.168.1.2");
        Node s3 = new Node("s3", "192.168.1.3");
        Node s4 = new Node("s4", "192.168.1.4");
        Node s5 = new Node("s5", "192.168.1.5");
        shards.add(s1);
        shards.add(s2);
        shards.add(s3);
        shards.add(s4);
        ConsistentHashing sh = new ConsistentHashing(shards);
        System.out.println("添加客户端，一开始有4个主机，分别为s1,s2,s3,s4,每个主机有100个虚拟主机：");
        sh.keyToNode("101客户端");
        sh.keyToNode("102客户端");
        sh.keyToNode("103客户端");
        sh.keyToNode("104客户端");
        sh.keyToNode("105客户端");
        sh.keyToNode("106客户端");
        sh.keyToNode("107客户端");
        sh.keyToNode("108客户端");
        sh.keyToNode("109客户端");

        sh.deleteS(s2);

        sh.addS(s5);

        System.out.println("最后的客户端到主机的映射为：");
        printKeyTree();
    }

    public static void printKeyTree() {
        for (Iterator<Long> it = treeKey.keySet().iterator(); it.hasNext();) {
            Long lo = it.next();
            System.out.println("hash(" + lo + ")连接到主机->" + treeKey.get(lo));
        }

    }

    private void init() { // 初始化一致性hash环
        nodes = new TreeMap<Long, Node>();
        treeKey = new TreeMap<Long, Node>();
        for (int i = 0; i != shards.size(); ++i) { // 每个真实机器节点都需要关联虚拟节点
            final Node shardInfo = shards.get(i);

            for (int n = 0; n < NODE_NUM; n++)
                // 一个真实机器节点关联NODE_NUM个虚拟节点
                nodes.put(hash("SHARD-" + shardInfo.name + "-NODE-" + n), shardInfo);
        }
    }

    //增加一个主机
    private void addS(Node s) {
        System.out.println("增加主机" + s + "的变化：");
        for (int n = 0; n < NODE_NUM; n++)
            addS(hash("SHARD-" + s.name + "-NODE-" + n), s);

    }

    //添加一个虚拟节点进环形结构,lg为虚拟节点的hash值
    public void addS(Long lg, Node s) {
        SortedMap<Long, Node> tail = nodes.tailMap(lg);
        SortedMap<Long, Node> head = nodes.headMap(lg);
        Long begin = 0L;
        Long end = 0L;
        SortedMap<Long, Node> between;
        if (head.size() == 0) {
            between = treeKey.tailMap(nodes.lastKey());
            flag = true;
        }
        else {
            begin = head.lastKey();
            between = treeKey.subMap(begin, lg);
            flag = false;
        }
        nodes.put(lg, s);
        for (Iterator<Long> it = between.keySet().iterator(); it.hasNext();) {
            Long lo = it.next();
            if (flag) {
                treeKey.put(lo, nodes.get(lg));
                System.out.println("hash(" + lo + ")改变到->" + tail.get(tail.firstKey()));
            }
            else {
                treeKey.put(lo, nodes.get(lg));
                System.out.println("hash(" + lo + ")改变到->" + tail.get(tail.firstKey()));
            }
        }
    }

    //删除真实节点是s
    public void deleteS(Node s) {
        if (s == null) {
            return;
        }
        System.out.println("删除主机" + s + "的变化：");
        for (int i = 0; i < NODE_NUM; i++) {
            //定位s节点的第i的虚拟节点的位置
            SortedMap<Long, Node> tail = nodes.tailMap(hash("SHARD-" + s.name + "-NODE-" + i));
            SortedMap<Long, Node> head = nodes.headMap(hash("SHARD-" + s.name + "-NODE-" + i));
            Long begin = 0L;
            Long end = 0L;

            SortedMap<Long, Node> between;
            if (head.size() == 0) {
                between = treeKey.tailMap(nodes.lastKey());
                end = tail.firstKey();
                tail.remove(tail.firstKey());
                nodes.remove(tail.firstKey());//从nodes中删除s节点的第i个虚拟节点
                flag = true;
            }
            else {
                begin = head.lastKey();
                end = tail.firstKey();
                tail.remove(tail.firstKey());
                between = treeKey.subMap(begin, end);//在s节点的第i个虚拟节点的所有key的集合
                flag = false;
            }
            for (Iterator<Long> it = between.keySet().iterator(); it.hasNext();) {
                Long lo = it.next();
                if (flag) {
                    treeKey.put(lo, tail.get(tail.firstKey()));
                    System.out.println("hash(" + lo + ")改变到->" + tail.get(tail.firstKey()));
                }
                else {
                    treeKey.put(lo, tail.get(tail.firstKey()));
                    System.out.println("hash(" + lo + ")改变到->" + tail.get(tail.firstKey()));
                }
            }
        }

    }

    //映射key到真实节点
    public void keyToNode(String key) {
        SortedMap<Long, Node> tail = nodes.tailMap(hash(key)); // 沿环的顺时针找到一个虚拟节点
        if (tail.size() == 0) {
            return;
        }
        treeKey.put(hash(key), tail.get(tail.firstKey()));
        System.out.println(key + "（hash：" + hash(key) + "）连接到主机->" + tail.get(tail.firstKey()));
    }

    /**
     * MurMurHash算法，是非加密HASH算法，性能很高，
     * 比传统的CRC32,MD5，SHA-1（这两个算法都是加密HASH算法，复杂度本身就很高，带来的性能上的损害也不可避免）
     * 等HASH算法要快很多，而且据说这个算法的碰撞率很低.
     * http://murmurhash.googlepages.com/
     */
    private static Long hash(String key) {

        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
        int seed = 0x1234ABCD;

        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }

}

class Node {
    String name;
    String ip;

    public Node(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    @Override
    public String toString() {
        return this.name + "-" + this.ip;
    }
}