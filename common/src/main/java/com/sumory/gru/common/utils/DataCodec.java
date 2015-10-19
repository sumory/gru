package com.sumory.gru.common.utils;

import in.srain.binpack.BinPack;

import java.util.Map;

/**
 * 编解码工具
 * 
 * @author sumory.wu
 * @date 2015年1月18日 下午3:52:07
 */
public class DataCodec {

    public static byte[] encode(Map<String, Object> obj) {
        return BinPack.encode(obj, "UTF-8");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decode(byte[] obj) {
        return (Map<String, Object>) BinPack.decode(obj, "UTF-8");
    }
}
