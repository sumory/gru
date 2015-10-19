package com.sumory.gru.common.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置
 * 
 * @author sumory.wu
 * @date 2015年1月21日 上午10:41:42
 */
public class Config {
    private static Properties properties;
    private static volatile boolean isLoad;

    static {
        synchronized (Config.class) {
            load();
        }
    }

    public static String get(String key) {
        if (!isLoad)
            load();
        return properties.getProperty(key);
    }

    public static Map<String, String> getConfig() {
        Map<String, String> result = new HashMap<>();
        if (!isLoad)
            load();
        Enumeration<String> e = (Enumeration<String>) properties.propertyNames();
        while (e.hasMoreElements()) {
            String k = e.nextElement();
            result.put(k, get(k));
        }
        return result;
    }

    private static void load() {
        properties = new Properties();
        try {
            properties.load(Config.class.getClassLoader().getResourceAsStream("system.properties"));
            isLoad = true;
        }
        catch (IOException e) {
            System.out.println("can not load the properties");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
