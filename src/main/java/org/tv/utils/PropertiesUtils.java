package org.tv.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesUtils {
    public static Properties load(String pathName) {
        Properties properties = new Properties();
        // 获取类加载器，用于加载资源文件
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // 通过类加载器获取配置文件的输入流

        try (InputStream inputStream = classLoader.getResourceAsStream(pathName)) {
            if (inputStream == null) {
                return properties;
            }

            // 创建 Properties 对象来加载配置文件
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            properties.load(inputStreamReader);

            // 现在可以从 Properties 对象中按键值对读取配置了
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }
}
