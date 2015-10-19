package com.sumory.gru.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.junit.Test;

import com.sumory.gru.common.utils.DateUtil;

public class DateUtilsTest {

    @Test
    public void testCodec() throws UnsupportedEncodingException {

        System.out.println(DateUtil.parseDateTimeFromString("2015-01-02 10:10:10"));
        System.out.println(DateUtil.parseDateTimeToMilliseconds("2015-01-02 10:10:10"));
        System.out.println(DateUtil.toDateTimeString(new Date()));
    }
}
