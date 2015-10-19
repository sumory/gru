package com.sumory.gru.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.sumory.gru.common.domain.StatObject;

/**
 * 用于整理统计的工具类，利用BitSet提高性能，减少超时
 * 
 * @author sumory.wu
 * @date 2015年4月20日 上午10:22:02
 */
public class BitSetUtil {

    public static StatObject buildFrom(long groupId, List<Integer> userIds, String extra) {
        if (userIds == null || userIds.isEmpty()) {
            return new StatObject(true, groupId, extra);
        }
        else {
            Collections.sort(userIds);
            int min = userIds.get(0);
            int max = userIds.get(userIds.size() - 1);
            //System.out.println(min + " " + max + " " + (max - min + 1));
            BitSet bs = new BitSet(max - min + 1);
            for (int i = 0; i < userIds.size(); i++) {
                bs.set(userIds.get(i) - min, true);
            }

            return new StatObject(false, groupId, extra, min, bs, object2Bytes(bs));
        }
    }

    public static StatObject buildFrom2(List<Integer> data) {//慢2倍多

        BitSet bs = new BitSet();
        for (int i = 0; i < data.size(); i++) {
            bs.set(data.get(i), true);
        }
        //    return new StatObject(0, bs);
        return null;
    }

    public static List<Integer> recoverFrom(StatObject result) {
        if (result == null || result.getBitSet() == null) {
            return null;
        }

        int base = result.getBase();
        BitSet bs = result.getBitSet();
        List<Integer> data = new ArrayList<Integer>();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            // operate on index i here
            //System.out.println(i + " " + bs.get(i));
            data.add(base + i);
        }
        return data;
    }

    /**
     * 字节转化为对象
     * 
     * @param bytes
     * @return
     */
    public static Object bytes2Object(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return null;
        try {
            ObjectInputStream inputStream;
            inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object obj = inputStream.readObject();
            return obj;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对象转化为字节
     * 
     * @param value
     * @return
     */
    public static byte[] object2Bytes(Object value) {
        if (value == null)
            return null;
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(arrayOutputStream);
            outputStream.writeObject(value);
            outputStream.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                arrayOutputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayOutputStream.toByteArray();
    }

    public static void main(String[] args) {
        System.out.println(Long.bitCount(123));
        BitSet bs = new BitSet();
        bs.set(1, true);
        bs.set(76543211, true);
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            // operate on index i here
            System.out.println(i + " " + bs.get(i));

        }

        int dataCount = 2000;
        Map<Integer, List<Integer>> dataMap = new HashMap<Integer, List<Integer>>();
        for (int c = 0; c < dataCount; c++) {
            List<Integer> ls = new ArrayList<Integer>();
            int max = new Random().nextInt(1234567890);
            if (max < 1000000)
                max = 1000002;
            int min = max - 1000000;
            for (int i = 0; i < 50; i++) {
                ls.add((int) (new Random().nextInt(max) % (max - min + 1) + min));
            }
            dataMap.put(c, ls);

            //Collections.sort(ls);
            // System.out.println(ls);
        }
        System.out.println("构造数据结束");
        long t0 = System.currentTimeMillis();

        for (int c = 0; c < dataCount; c++) {

            long t1 = System.currentTimeMillis();
            StatObject result = buildFrom(1, dataMap.get(c), "extra");

            List<Integer> lsIntegers = recoverFrom(result);
            //System.out.println(lsIntegers);
            long t2 = System.currentTimeMillis();
            //            if (t2 - t1 > 100)
            //                System.out.println(t2 - t1);
        }
        long t00 = System.currentTimeMillis();
        System.out.println("耗时1：" + (t00 - t0));

        //        long t9 = System.currentTimeMillis();
        //
        //        for (int c = 0; c < dataCount; c++) {
        //
        //            long t3 = System.currentTimeMillis();
        //            Result result = buildFrom2(dataMap.get(c));
        //            //System.out.println(recoverFrom(bb));
        //            long t4 = System.currentTimeMillis();
        //            //            if (t4 - t3 > 100)
        //            //                System.out.println(t4 - t3);
        //        }
        //        long t99 = System.currentTimeMillis();
        //        System.out.println("耗时2：" + (t99 - t9));
    }
}
