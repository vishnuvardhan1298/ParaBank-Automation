package com.fintech.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties props = new Properties();

    static {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            props.load(fis);
        } catch (Exception e) {
            System.out.println("Failed to load config: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
    public static String get(String key, String defaultValue) {
        String value = props.getProperty(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

}
