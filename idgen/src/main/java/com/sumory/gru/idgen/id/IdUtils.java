package com.sumory.gru.idgen.id;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.sumory.gru.common.config.Config;

/**
 * id生成工具
 * 
 * @author sumory.wu
 * @date 2015年3月13日 下午7:15:32
 */
public class IdUtils {
    private static final Logger logger = LoggerFactory.getLogger(IdUtils.class);

    static IdWorker msgIdWorker = new IdWorker(Integer.parseInt(Config.get("id.generator.worker")));

    public static long genMsgId() throws Exception {
        try {
            long genId = msgIdWorker.nextId();
            logger.info("当前workerId为：" + msgIdWorker.getWorkerId() + " 生成id：" + genId);
            return genId;
        }
        catch (Exception e) {
            logger.error("生成msg id发生异常", e);
            throw e;
        }
    }
}