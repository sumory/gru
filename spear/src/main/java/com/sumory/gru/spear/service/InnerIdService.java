package com.sumory.gru.spear.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumory.gru.idgen.id.IdUtils;
import com.sumory.gru.idgen.service.IdService;

/**
 * 最简化部署single模式时使用的本地id生成服务
 * 
 * @author sumory.wu
 * @date 2015年10月25日 下午8:38:09
 */
public class InnerIdService implements IdService {
    private final static Logger logger = LoggerFactory.getLogger(InnerIdService.class);

    @Override
    public String getServiceVersion() {
        return "inner";
    }

    @Override
    public long getMsgId() {
        try {
            return IdUtils.genMsgId();
        }
        catch (Exception e) {
            logger.error("inner id service generate id error", e);
            return Long.MIN_VALUE;
        }
    }
}
