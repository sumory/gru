package com.sumory.gru.spear.main;

import java.util.Map;

import com.sumory.gru.spear.extention.IAck;
import com.sumory.gru.spear.extention.LogAck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sumory.gru.idgen.service.IdService;
import com.sumory.gru.spear.SpearContext;
import com.sumory.gru.spear.server.SpearServer;
import com.sumory.gru.spear.monitor.MonitorServer;
import com.sumory.gru.spear.service.InnerIdService;
import com.sumory.gru.spear.service.InnerStatService;
import com.sumory.gru.spear.task.UserStat;
import com.sumory.gru.spear.transport.IReceiver;
import com.sumory.gru.spear.transport.ISender;
import com.sumory.gru.spear.transport.inner.InnerReceiver;
import com.sumory.gru.spear.transport.inner.InnerSender;
import com.sumory.gru.spear.transport.redis.RedisReceiver;
import com.sumory.gru.spear.transport.redis.RedisSender;
import com.sumory.gru.spear.transport.rocketmq.RocketMQReceiver;
import com.sumory.gru.spear.transport.rocketmq.RocketMQSender;
import com.sumory.gru.spear.zk.ZkUtil;
import com.sumory.gru.stat.service.StatService;

/**
 * 启动入口
 *
 * @author sumory.wu
 * @date 2015年1月14日 下午8:07:00
 */
public class SpearMain {
    private final static Logger logger = LoggerFactory.getLogger(SpearMain.class);
    private final static String DEFAULT_MODE = "single";//集群节点间通信模式、集群部署模式，详见配置文件描述

    public static void main(String[] args) {
        final SpearContext context = SpearContext.getInstance();
        final Map<String, String> config = context.getConfig();
        final IdService idService;
        final StatService statService;
        final SpearServer spearServer;

        try {
            if (DEFAULT_MODE.equals(config.get("mode"))) {//最小化部署single模式时使用本地实现的两个service
                idService = new InnerIdService();
                //最小化模式不提供stat服务，所以这个接口没有具体实现，后面也不会被调用到
                statService = new InnerStatService();
            } else {//开启了集群模式服务
                ApplicationContext appContext = new ClassPathXmlApplicationContext(new String[]{
                        "applicationContext.xml",
                        "applicationContext-consumer-idgen.xml",
                        "applicationContext-consumer-stat.xml"});
                idService = (IdService) appContext.getBean("idService");
                statService = (StatService) appContext.getBean("statService");
            }

            logger.info("idgen service version:{}, stat service version:{}", idService.getServiceVersion(),
                    statService.getServiceVersion());
            context.setIdService(idService);
            context.setStatService(statService);
        } catch (Exception e) {
            logger.error("spear服务启动出错", e);
            System.exit(-1);
        }

        //需要ack的情况，创建ack
        try {
            IAck ack = null;
            String ackType = config.get("ack") != null ? config.get("ack") : "";
            switch (ackType) {
                case "":
                    break;
                case "log":
                    ack = new LogAck();
                    break;
                case "rabbitmq":
                    //ack = new RabbitMQAck();
                    break;
                default:
                    ack = null;
            }
            logger.info("ack模式为:{}", ackType);
            context.setAck(ack);
        } catch (Exception e) {
            logger.error("设置ack出错", e);
            System.exit(-1);
        }

        //启动队列服务，启动节点长连接服务
        try {
            ISender sender;
            IReceiver receiver;
            String mode = config.get("mode");
            switch (mode) {
                case DEFAULT_MODE:
                    sender = new InnerSender(context);
                    receiver = new InnerReceiver(context);
                    break;
                case "redis":
                    sender = new RedisSender(context);
                    receiver = new RedisReceiver(context);
                    break;
                case "rocketmq":
                    sender = new RocketMQSender(context);
                    receiver = new RocketMQReceiver(context);
                    break;
                default:
                    sender = new InnerSender(context);
                    receiver = new InnerReceiver(context);
            }

            context.setSender(sender);
            context.setReceiver(receiver);
            spearServer = new SpearServer(context);

            sender.run();
            spearServer.run();

            boolean monitorStart = Boolean.parseBoolean(config.get("monitor.start"));
            if (monitorStart) {
                MonitorServer monitorServer = new MonitorServer(context);
                monitorServer.run();
            }
        } catch (Exception e) {
            logger.error("spear服务启动出错", e);
            System.exit(-1);
        }


        //注册本节点到zookeeper，启动节点信息上报服务
        if (!DEFAULT_MODE.equals(config.get("mode"))) {
            try {
                //启动zk，注册本节点
                int sessionTimeout = 3000;
                int retryTimes = 10;
                String zkHost = config.get("zk.addr");
                String baseNode = config.get("zk.spear.cluster");
                String outAddr = config.get("out.addr");//每个spear节点此值需唯一
                String spearId = config.get("spear.id");//每个spear节点此值需唯一
                ZkUtil.initNode(zkHost, baseNode, outAddr, spearId, sessionTimeout, retryTimes);

                //启动节点信息统计上报服务
                UserStat userStat = new UserStat(context);
                userStat.run();
            } catch (Exception e) {
                logger.error("在zk上注册服务、开启上报服务出错", e);
                System.exit(-1);
            }
        }

        //死锁等待
        synchronized (SpearMain.class) {
            while (true) {
                try {
                    SpearMain.class.wait();
                } catch (Throwable e) {
                }
            }
        }
    }
}
