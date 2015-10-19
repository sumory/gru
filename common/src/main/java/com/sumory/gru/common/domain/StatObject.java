package com.sumory.gru.common.domain;

import java.io.Serializable;
import java.util.BitSet;

/**
 * 群组状态统计
 * 
 * @author sumory.wu
 * @date 2015年4月20日 下午3:11:18
 */
public class StatObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isEmpty;//bitSet是否为空，即是否有学生
    private long groupId;
    private int base;//bitset中所有值的基值，如base为5，bitset里存的是1，2，3，那么实际值则是6，7，8
    private String extra;//扩展数据
    private transient BitSet bitSet;//dubbo默认的hessian无法序列化BitSet，所以用bytes[]代替，直接不序列化该字段
    private byte[] bitSetBytes;//dubbo无法序列化BitSet，这里先转化为字节处理

    public StatObject() {

    }

    public StatObject(boolean isEmpty, long groupId, String extra) {
        this.isEmpty = isEmpty;
        this.groupId = groupId;
        this.extra = extra;
    }

    public StatObject(boolean isEmpty, long groupId, String extra, int base, BitSet bitSet,
            byte[] bitSetBytes) {
        this.isEmpty = isEmpty;
        this.groupId = groupId;
        this.base = base;
        this.bitSet = bitSet;
        this.extra = extra;
        this.bitSetBytes = bitSetBytes;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public void setBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public byte[] getBitSetBytes() {
        return bitSetBytes;
    }

    public void setBitSetBytes(byte[] bitSetBytes) {
        this.bitSetBytes = bitSetBytes;
    }

}
