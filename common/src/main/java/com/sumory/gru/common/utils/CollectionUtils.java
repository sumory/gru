package com.sumory.gru.common.utils;

import java.util.Collection;
import java.util.Map;

public class CollectionUtils {
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
}
