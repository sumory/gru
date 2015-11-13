package com.sumory.gru.stat.main;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.container.Container;
import com.sumory.gru.common.config.Config;
import com.sumory.gru.stat.context.StatContext;
import com.sumory.gru.stat.servlet.StatServlet;
import com.sumory.gru.stat.zk.ZkUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatMain {
    private static final Logger logger = LoggerFactory.getLogger(StatMain.class);

    public static final String CONTAINER_KEY = "dubbo.container";
    public static final String SHUTDOWN_HOOK_KEY = "dubbo.shutdown.hook";
    private static final ExtensionLoader<Container> loader = ExtensionLoader
            .getExtensionLoader(Container.class);
    private static volatile boolean running = true;

    public static void main(String[] args) {
        //stat业务启动
        try {
            StatContext context = StatContext.getInstance();
            //启动zk监听
            String zkHost = Config.get("zk.addr");
            String baseNode = Config.get("zk.spear.cluster");
            int sessionTimeout = 3000;
            int retryTimes = 10;
            ZkUtil.initListener(zkHost, baseNode, sessionTimeout, retryTimes, context);
        } catch (Exception e) {
            logger.error("zk监听异常", e);
            System.exit(1);
        }

        //dubbo启动
        try {
            if (args == null || args.length == 0) {
                String config = ConfigUtils.getProperty(CONTAINER_KEY,
                        loader.getDefaultExtensionName());
                args = Constants.COMMA_SPLIT_PATTERN.split(config);
            }

            final List<Container> containers = new ArrayList<Container>();
            for (int i = 0; i < args.length; i++) {
                containers.add(loader.getExtension(args[i]));
            }
            logger.info("Use container type(" + Arrays.toString(args) + ") to run dubbo serivce.");

            if ("true".equals(System.getProperty(SHUTDOWN_HOOK_KEY))) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        for (Container container : containers) {
                            try {
                                container.stop();
                                logger.info("Dubbo " + container.getClass().getSimpleName()
                                        + " stopped!");
                            } catch (Throwable t) {
                                logger.error(t.getMessage(), t);
                            }
                            synchronized (StatMain.class) {
                                running = false;
                                StatMain.class.notify();
                            }
                        }
                    }
                });
            }

            for (Container container : containers) {
                container.start();
                logger.info("Dubbo " + container.getClass().getSimpleName() + " started!");
            }
            logger.info("Dubbo gru-stat-service server started!");
        } catch (RuntimeException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            System.exit(1);
        }

        //http api, 给监控minions使用的接口
        try {
            Server server = new Server(Integer.parseInt(Config.get("http.port")));
            ServletContextHandler context = new ServletContextHandler(
                    ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            context.addServlet(new ServletHolder(new StatServlet()), "/*");
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        synchronized (StatMain.class) {
            while (running) {
                try {
                    StatMain.class.wait();
                } catch (Throwable e) {
                }
            }
        }
    }
}
