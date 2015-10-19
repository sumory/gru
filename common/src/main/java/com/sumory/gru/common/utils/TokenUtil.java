package com.sumory.gru.common.utils;

import java.security.MessageDigest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenUtil {
    private final static Logger logger = LoggerFactory.getLogger(TokenUtil.class);

    public static String genToken(String source, String salt) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(salt))
            return "";

        String str = source + "_" + salt;
        byte[] buf = str.getBytes();
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(buf);
            byte[] hash = md5.digest();
            String d = "";
            int usbyte = 0; // unsigned byte
            for (int i = 0; i < hash.length; i += 2) { // format with 2-byte words with spaces.
                usbyte = hash[i] & 0xFF;
                if (usbyte < 16)
                    d += "0" + Integer.toHexString(usbyte);
                else
                    d += Integer.toHexString(usbyte);
                usbyte = hash[i + 1] & 0xFF;
                if (usbyte < 16)
                    d += "0" + Integer.toHexString(usbyte);
                else
                    d += Integer.toHexString(usbyte); // + " ";
            }
            return d.trim().toLowerCase();
        }
        catch (Exception e) {
            logger.error("生成token出错", e);
            return "";
        }
    }

    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            System.out.println(genToken("123456", "salt"));//397a79aca9daa9aa6b06dfd834e23c81
            System.out.println(genToken("123456我是谁", "salt"));//05449c97faaf7df7efe43cefc6c37631
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);

        System.out.println(genToken("652_mock_student_652", "token_gen_for_ticket@sumory.com"));
        System.out.println(genToken("652_mock_student_652_01500389b27446edc335fa6a68281cdb",
                "token_gen_for_spear@sumory.com"));
    }
}
