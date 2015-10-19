package com.sumory.gru.ticket.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一致性hash，用于hash连接到spear节点
 * 
 * @author sumory.wu
 * @date 2015年3月10日 下午7:00:15
 */
public class Shard<T extends Node> { // T封装了机器节点的信息，如name、addr等
    private static final Logger logger = LoggerFactory.getLogger(Shard.class);

    private SortedMap<Long, T> nodes; // 虚拟节点到真实节点的映射，key为虚拟节点hash，value为真实节点
    private final int NODE_NUM = 5; // 每个机器节点关联的虚拟节点个数
    private ConcurrentHashMap<String, T> shards;//用于保存真实节点，String为T.addr，各真实节点此值需唯一

    public Shard() {
        this.nodes = Collections.synchronizedSortedMap(new TreeMap<Long, T>());
        this.shards = new ConcurrentHashMap<String, T>();
        initCircle();
    }

    public Shard(List<T> shards) {
        this.nodes = Collections.synchronizedSortedMap(new TreeMap<Long, T>());
        this.shards = new ConcurrentHashMap<String, T>();
        if (shards != null && shards.size() > 0) {
            for (T t : shards) {
                this.shards.put(t.getAddr(), t);
            }
        }

        initCircle();
    }

    /**
     * 初始化
     * 
     * @author sumory.wu @date 2015年3月12日 下午2:42:30
     */
    private void initCircle() {
        Iterator<Entry<String, T>> it = shards.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, T> e = it.next();
            T t = e.getValue();

            // 一个真实机器节点关联NODE_NUM个虚拟节点
            for (int n = 0; n < NODE_NUM; n++)
                nodes.put(hash("SHARD-" + t.addr + "-NODE-" + n), t);
        }

        //        for (int i = 0; i != shards.size(); ++i) { // 每个真实机器节点都需要关联虚拟节点
        //            final T shardInfo = shards.get(i);
        //
        //            for (int n = 0; n < NODE_NUM; n++)
        //                // 一个真实机器节点关联NODE_NUM个虚拟节点
        //                nodes.put(hash("SHARD-" + shardInfo.addr + "-NODE-" + n), shardInfo);
        //        }
    }

    /**
     * 添加一个真实节点
     * 
     * @author sumory.wu @date 2015年3月12日 下午2:39:18
     * @param s
     */
    public void addNode(T s) {
        System.out.println("增加主机" + s + "的变化：");
        for (int n = 0; n < NODE_NUM; n++)
            addVirtual(hash("SHARD-" + s.addr + "-NODE-" + n), s);

        System.out.println("添加key" + s.getAddr());
        shards.put(s.getAddr(), s);
    }

    /**
     * 添加一个虚拟节点
     * 
     * @author sumory.wu @date 2015年3月12日 下午2:39:28
     * @param lg 虚拟节点hash
     * @param s 真实节点
     */
    private void addVirtual(Long lg, T s) {
        nodes.put(lg, s);

    }

    /**
     * 删除一个真实节点
     * 
     * @author sumory.wu @date 2015年3月12日 下午2:40:02
     * @param s
     */
    public void deleteNode(T s) {
        if (s == null) {
            return;
        }

        System.out.println("删除主机" + s + "的变化：");
        synchronized (nodes) {
            for (int i = 0; i < NODE_NUM; i++) {
                nodes.remove(hash("SHARD-" + s.addr + "-NODE-" + i));//从nodes中删除s节点的第i个虚拟节点
            }
        }

        if (shards.containsKey(s.getAddr()))
            shards.remove(s.getAddr());
        else {
            System.out.println("不含有key" + s.getAddr());
        }
    }

    /**
     * 重设真实节点
     * 
     * @author sumory.wu @date 2015年3月12日 下午2:50:01
     * @param nodes 重新初始化的节点
     */
    public synchronized void resetNodes(List<T> nodes) {
        this.nodes = Collections.synchronizedSortedMap(new TreeMap<Long, T>());
        this.shards = new ConcurrentHashMap<String, T>();
        for (T t : nodes) {
            this.shards.put(t.getAddr(), t);
        }

        initCircle();
    }

    /**
     * 获取某个key值应hash到的真实节点
     * 
     * @author sumory.wu @date 2015年3月12日 下午2:40:14
     * @param key
     * @return
     */
    public T getNode(String key) {
        try {
            T n = null;
            // synchronized (nodes) {
            if (nodes.isEmpty())
                return null;
            SortedMap<Long, T> tail = nodes.tailMap(hash(key)); // 沿环的顺时针找到一个虚拟节点
            if (tail.isEmpty()) {//找不到时查找headMap，否则会出现null
                SortedMap<Long, T> head = nodes.headMap(hash(key));
                if (head.isEmpty())
                    return null;
                n = head.get(head.firstKey());
                // System.out.println("\t（hash：" + hash(key) + "）re连接到主机->\t" + n.name);
            }
            else {
                n = tail.get(tail.firstKey());
                // System.out.println("\t（hash：" + hash(key) + "）连接到主机->\t" + n.name);
            }
            //}
            return n;
        }
        catch (Exception e) {
            logger.error("获取对应的真实节点出错", e);
            return null;
        }

    }

    public ConcurrentHashMap<String, T> getShards() {
        return shards;
    }

    /**
     * 打印真实节点
     * 
     * @author sumory.wu @date 2015年3月12日 下午2:43:08
     */
    public void printNodes() {
        Set<Long> s = nodes.keySet(); // 取keyset不用加锁
        synchronized (nodes) { // 锁nodes，而不是s
            Iterator<Long> i = s.iterator(); // 需加锁 
            while (i.hasNext()) {
                long key = i.next();
                T value = nodes.get(key);
                System.out.println(key + "\t" + value.name);
            }
        }
    }

    public void printShards() {
        Iterator<Entry<String, T>> it = shards.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, T> e = it.next();
            System.out.println("key:" + e.getKey() + " value:" + e.getValue());
        }

    }

    /**
     * MurMurHash算法，是非加密HASH算法，性能很高，
     * 比传统的CRC32,MD5，SHA-1（这两个算法都是加密HASH算法，复杂度本身就很高，带来的性能上的损害也不可避免）
     * 等HASH算法要快很多
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
