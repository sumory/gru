package com.sumory.gru.common.id;

import java.util.HashSet;
import java.util.Set;

/**
 * 任意进制(2~62)转换
 * 
 * @author sumory.wu
 * @ref https://github.com/sumory/baseN4j
 * @date 2015年3月2日 下午6:04:13
 */
public class BaseN {

    private Character[] defaultBase = new Character[] { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z' };
    private long maxNum = Long.MAX_VALUE;// 1L << 63-1
    private Character[] base;
    private int radix;

    public BaseN() {
        this.base = defaultBase;
        this.radix = this.base.length;
    }

    public BaseN(int radix) throws Exception {
        if (radix > 62 || radix < 2) {
            throw new Exception(
                    "Error param: if the param is numeric, it must be between 2 and 62.");
        }
        Character[] newBase = new Character[radix];
        for (int i = 0; i < radix; i++) {
            newBase[i] = defaultBase[i];
        }
        this.base = newBase;
        this.radix = radix;
    }

    public BaseN(Character[] base) {
        this.base = base;
        this.radix = base.length;
    }

    public void reBase(int radix) throws Exception {
        if (radix > this.defaultBase.length) {
            throw new Exception("Error param: radix must be <= 62.");
        }

        Character[] newBase = new Character[radix];
        Character[] defaultArray = this.defaultBase;

        Set<Character> ts = new HashSet<Character>();
        while (ts.size() < radix) {
            int n = (int) (Math.random() * defaultArray.length);
            ts.add(defaultArray[n]);
        }
        ts.toArray(newBase);

        this.base = newBase;
        this.radix = radix;
    }

    public String encode(long num) {
        if (num > this.maxNum) {
            return "-";
        }

        String tmp = "";
        String result = "";
        boolean negative = false;

        if (num == 0) {
            return this.base[0].toString();
        }
        if (num < 0) {
            negative = true;
            num = -num;
        }

        while (num > 0) {
            tmp += this.base[(int) (num % this.radix)];
            num = (long) (num / this.radix);
        }

        for (int i = tmp.length() - 1; i >= 0; i--) {
            result += tmp.charAt(i);
        }

        return negative ? '-' + result : result;
    }

    public long decode(String str) {
        long result = 0;
        boolean negative = false;

        if (str.indexOf('-') == 0) {
            negative = true;
            str = str.substring(1);
        }

        for (int index = 0; index < str.length(); index++) {
            char c = str.charAt(index);
            int tableIndex = 0;
            for (int i = 0; i < this.base.length; i++) {
                if (this.base[i].charValue() == c) {
                    tableIndex = i;
                    break;
                }
            }

            result += tableIndex * Math.pow(this.radix, str.length() - index - 1);
        }

        return negative ? result * (-1) : result;
    }

    public static void main(String[] args) throws Exception {

        BaseN baseN = new BaseN();
        int radix = 10;
        baseN.reBase(radix);

        for (int i = 0; i < radix; i++) {
            // System.out.println(baseN.base[i]);
        }

        for (int i = 64990; i < 65000; i++) {
            System.out.print("i " + i);
            System.out.print("  encode " + baseN.encode(i));
            System.out.print("  decode " + baseN.decode(baseN.encode(i)));
            System.out.println();
        }

    }
}