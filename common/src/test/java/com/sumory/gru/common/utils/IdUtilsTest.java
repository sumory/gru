package com.sumory.gru.common.utils;

import junit.framework.Assert;

import org.junit.Test;

import com.sumory.gru.common.utils.IdUtil;

public class IdUtilsTest {

    @Test
    public void test() throws Exception {
        for (int i = 0; i < 100; i++) {
            long id = IdUtil.generateMsgId();
            System.out.println(id);
            Assert.assertNotNull(id);
        }
    }
}
