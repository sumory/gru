package com.sumory.gru.ticket.common.test;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sumory.gru.ticket.common.Node;
import com.sumory.gru.ticket.common.Shard;

public class ShardTest {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ShardTest.class);

    public static void testMultiThread() {
        final List<String> clients = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            clients.add("客户端" + (100 + i));
        }

        List<Node> shards = new ArrayList<Node>(); // 真实机器节点

        for (int i = 0; i < 3; i++) {
            Node s1 = new Node("s" + i, "192.168.1." + i + ":" + i + "000");
            shards.add(s1);
        }
        final Shard<Node> sh = new Shard<Node>(shards);
        sh.printNodes();
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%");

        for (int j = 0; j < 3; j++) {
            final int m = j;
            Thread sThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {

                        if (m == 0) {
                            sh.addNode(new Node("node" + 1, "10.60.0." + 1));
                            sh.printShards();
                            // synchronized (this) {
                            for (int i = 0; i < 15; i++) {
                                sh.getNode("客户端" + (100 + i));
                            }
                            // }

                            try {
                                Thread.sleep(new Random().nextInt(2000));
                            }
                            catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (m == 1) {
                            sh.addNode(new Node("node" + 2, "10.60.0." + 2));
                            sh.deleteNode(new Node("node" + 1, "10.60.0." + 1));
                            sh.printShards();
                            // synchronized (this) {
                            for (int i = 0; i < 15; i++) {
                                sh.getNode("客户端" + (100 + i));
                            }
                            // }
                            try {
                                Thread.sleep(new Random().nextInt(3000));
                            }
                            catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (m == 2) {
                            sh.deleteNode(new Node("node" + 2, "10.60.0." + 2));

                            try {
                                Thread.sleep(new Random().nextInt(3000));
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }

            });
            sThread.start();
        }
    }

    public static void testCommonFunction() {
        final List<String> clients = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            clients.add("客户端" + (100 + i));
        }

        List<Node> shards = new ArrayList<Node>(); // 真实机器节点

        for (int i = 0; i < 3; i++) {
            Node s1 = new Node("s" + i, "192.168.1." + i + ":" + i + "000");
            shards.add(s1);
        }
        final Shard<Node> sh = new Shard<Node>(shards);
        sh.printNodes();
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%");

        System.out.println("+++++++++++++++++++++++++++++++++");
        for (int i = 0; i < clients.size(); i++) {
            sh.getNode(clients.get(i));
        }
        sh.printNodes();

        System.out.println("+++++++++++++++++++++++++++++++++");
        sh.deleteNode(shards.get(0));
        for (int i = 0; i < clients.size(); i++) {
            sh.getNode(clients.get(i));
        }
        sh.printNodes();

        System.out.println("+++++++++++++++++++++++++++++++++");

        Node s8 = new Node("s8", "192.168.1.8:8000");
        sh.addNode(s8);
        for (int i = 0; i < clients.size(); i++) {
            sh.getNode(clients.get(i));
        }
        sh.printNodes();
    }

    public static void main(String[] args) {
        testMultiThread();
    }
}
