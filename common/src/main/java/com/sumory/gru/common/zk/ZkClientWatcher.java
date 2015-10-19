package com.sumory.gru.common.zk;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zookeeper watcher，包装zookeeper访问API，获取最新存活的spear nodes，并监听变化
 * 
 * @author sumory.wu
 * @date 2015年3月8日 下午4:33:19
 */
public abstract class ZkClientWatcher implements Watcher {
    private static final Logger logger = LoggerFactory.getLogger(ZkClientWatcher.class);

    private String zkHost;
    private String baseNode;
    private int sessionTimeout;
    private int connectRetryTimes;
    private NotifyListener listener;
    private ZooKeeper zooKeeper;

    public ZkClientWatcher(String zkHost, String baseNode, int sessionTimeout, int retryTimes,
            NotifyListener listener) throws IOException {
        this.zkHost = zkHost;
        this.baseNode = baseNode;
        this.sessionTimeout = sessionTimeout;
        this.connectRetryTimes = retryTimes;
        this.listener = listener;
        connectZooKeeper();
    }

    /**
     * 建立连接成功后需要调用listener，执行自定义操作
     * 
     * @author sumory.wu @date 2015年3月8日 下午4:33:49
     * @throws IOException
     */
    private void connectZooKeeper() throws IOException {
        for (int i = 0; i <= connectRetryTimes; i++) {
            try {
                zooKeeper = new ZooKeeper(zkHost, sessionTimeout, this);
                logger.info("已连接到zookeeper {} ", zkHost);
                break;
            }
            catch (IOException e) {
                if (i == connectRetryTimes) {
                    throw new IOException("多次尝试后仍无法连接zookeeper", e);
                }
                logger.info("连接zookeeper发生异常, 已尝试 {} 次", (i + 1));
            }
        }
        try {
            if (zooKeeper != null && listener != null)
                listener.action(zooKeeper);
            else
                logger.error("zooKeepern或listener为空，{}", (zooKeeper != null) ? "若无需listener，请忽略"
                        : "zookeeper为空，未初始化，请检查");

        }
        catch (Exception e) {
            logger.error("往zookeeper注册信息出错", e);
        }
    }

    private void reconnectZooKeeper() throws InterruptedException, IOException {
        logger.info("尝试重连ZooKeeper:{}", zkHost);

        if (zooKeeper != null) {
            zooKeeper.close();
        }
        connectZooKeeper();
    }

    @Override
    public void process(WatchedEvent event) {
        logger.info("收到zookeeper事件, " + "type=" + event.getType() + ", " + "state="
                + event.getState() + ", " + "path=" + event.getPath());
        //        try {
        //            zooKeeper.exists(this.baseNode, true);
        //        }
        //        catch (Exception e) {
        //            e.printStackTrace();
        //        }

        switch (event.getType()) {
        case None: {
            switch (event.getState()) {
            case SyncConnected: {
                try {
                    waitToInitZooKeeper(5000);
                }
                catch (Exception e) {
                    logger.error("Error to init ZooKeeper object after sleeping some time, reconnect ZooKeeper");
                    try {
                        reconnectZooKeeper();
                    }
                    catch (Exception e1) {
                        logger.error("Error to reconnect with ZooKeeper", e1);
                    }
                }
                break;
            }
            case Expired: {
                try {
                    logger.info("expired and reconnect, need to register some business data");
                    //Done: 需要重写本节点信息
                    reconnectZooKeeper();
                }
                catch (Exception e1) {
                    logger.error("Error to reconnect with ZooKeeper", e1);
                }
                break;
            }
            default:
                break;
            }
            break;
        }
        case NodeCreated: {
            logger.info(event.getPath() + " created");
            processNodeCreated(event.getPath());
            break;
        }
        case NodeDeleted: {
            logger.info(event.getPath() + " deleted");
            processNodeDeleted(event.getPath());
            break;
        }
        case NodeDataChanged: {
            logger.info(event.getPath() + " data changed");
            processDataChanged(event.getPath());
            break;
        }
        case NodeChildrenChanged: {
            logger.info(event.getPath() + " children changed");
            processNodeChildrenChanged(event.getPath());
            break;
        }
        default:
            logger.info("Other event type, path：{}，type：{}", event.getPath(), event.getType()
                    .toString());
            break;
        }
    }

    protected abstract void processNodeCreated(String path);

    protected abstract void processNodeDeleted(String path);

    protected abstract void processDataChanged(String path);

    protected abstract void processNodeChildrenChanged(String path);

    public boolean createEphemeralNode(String node, byte[] data) throws Exception {

        try {
            logger.info("Try to create emphemeral znode " + baseNode);
            System.out.println(zooKeeper);
            getZooKeeper().create(node, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        }
        catch (KeeperException.NodeExistsException nee) {
            logger.error("create error", nee);
            return false;
        }
        catch (InterruptedException e) {
            logger.info("Interrupted", e);
            Thread.currentThread().interrupt();
        }
        return true;
    }

    /**
     * 等待一段时间初始化zk，若仍为null则抛出异常
     * 
     * @author sumory.wu @date 2015年3月7日 下午9:47:56
     * @param maxWaitMillis
     * @throws Exception
     */
    public void waitToInitZooKeeper(long maxWaitMillis) throws Exception {
        logger.info("waitToInitZooKeeper.......");
        long finished = System.currentTimeMillis() + maxWaitMillis;
        while (System.currentTimeMillis() < finished) {
            if (this.zooKeeper != null) {
                return;
            }

            try {
                Thread.sleep(1);
            }
            catch (InterruptedException e) {
                throw new Exception(e);
            }
        }
        throw new Exception();
    }

    public byte[] getData(ZkClientWatcher zkClientWatcher, String znode) throws IOException {
        byte[] data = null;
        for (int i = 0; i <= connectRetryTimes; i++) {
            try {
                data = zkClientWatcher.getZooKeeper().getData(znode, null, null);
                break;
            }
            catch (Exception e) {
                logger.info("Exceptioin to get data from ZooKeeper, retry " + i + " times");
                if (i == connectRetryTimes) {
                    throw new IOException("Error when getting data from " + znode
                            + " after retrying");
                }
            }
        }
        return data;
    }

    public void close() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            }
            catch (InterruptedException e) {
                logger.error("Interrupt to close zookeeper connection", e);
            }
        }
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public String getBaseNode() {
        return baseNode;
    }

}
