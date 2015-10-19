package com.sumory.gru.idgen.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sumory.gru.idgen.id.IdUtils;
import com.sumory.gru.idgen.service.IdService;

/**
 * id生成服务
 * 
 * @author sumory.wu
 * @date 2015年3月15日 上午11:16:14
 */
@Service(value = "idService")
public class IdServiceImpl implements IdService {
    private static final Logger logger = LoggerFactory.getLogger(IdServiceImpl.class);

    @Value("${dubbo.gru.idgen.version}")
    private String version;

    @Override
    public String getServiceVersion() {
        return version;
    }

    /**
     * 生成消息id
     */
    @Override
    public long getMsgId() {
        try {
            return IdUtils.genMsgId();
        }
        catch (Exception e) {
            logger.error("id生成出现严重异常", e);
            return Long.MIN_VALUE;
        }
    }
}
