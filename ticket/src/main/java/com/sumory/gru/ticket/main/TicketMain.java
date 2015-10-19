package com.sumory.gru.ticket.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.common.config.Config;
import com.sumory.gru.ticket.common.Node;
import com.sumory.gru.ticket.common.Shard;
import com.sumory.gru.ticket.server.TicketServer;
import com.sumory.gru.ticket.zk.ZkUtil;

/**
 * ticket server启动入口
 * 
 * @author sumory.wu
 * @date 2015年3月11日 上午9:41:00
 */
public class TicketMain {
    private static final Logger logger = LoggerFactory.getLogger(TicketMain.class);

    public static Shard<Node> shard = new Shard<Node>();

    public static void main(String[] args) throws Exception {
        try {

            //启动zk监听
            String zkHost = Config.get("zk.addr");
            String baseNode = Config.get("zk.spear.cluster");
            int sessionTimeout = 3000;
            int retryTimes = 10;
            ZkUtil.initListener(zkHost, baseNode, sessionTimeout, retryTimes, shard);

            //启动ticket server接收http请求
            final TicketServer ns = new TicketServer();
            ns.startHttp();
        }
        catch (Exception e) {
            logger.error("启动ticket server异常", e);
        }

    }

}
