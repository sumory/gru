package com.sumory.gru.spear.extention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogAck<String> implements IAck<String> {
    private final static Logger logger = LoggerFactory.getLogger(LogAck.class);

    @Override
    public void ack(String d) {
        logger.info("Log Ack:{}", d);
    }
}
