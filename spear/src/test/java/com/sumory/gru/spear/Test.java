package com.sumory.gru.spear;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * 
 * @author sumory.wu
 * @date 2015年1月14日 下午8:25:27
 */
public class Test {
    public static void main(String[] args) {

        Integer[] abc = new Integer[] { 1, 2, 3, 4, 99, 7 };
        List<Integer> list = Arrays.asList(abc);
        Collections.sort(list);
        System.out.println(list.get(list.size() - 1));

        List<Object> ol = null;
        for (Object o : ol) {
            System.out.println(o);
        }
    }
}
