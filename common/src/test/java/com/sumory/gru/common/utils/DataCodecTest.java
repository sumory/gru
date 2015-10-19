package com.sumory.gru.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sumory.gru.common.utils.DataCodec;

public class DataCodecTest {

    @Test
    public void testCodec() throws UnsupportedEncodingException {
        String strToBs = "All men are created equal.";
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("abc", 32);
        m.put("hello", "You jump, I jump");
        m.put("world", strToBs.getBytes("UTF-8"));
        m.put("float", 12345.6789E123);

        byte[] tmp = DataCodec.encode(m);
        Map<String, Object> obj = (Map<String, Object>) DataCodec.decode(tmp);
        byte[] bs = (byte[]) obj.get("world");
        System.out.println(m);
        System.out.println(obj);
        System.out.println(new String(bs, "UTF-8").equals(strToBs));
    }
}
