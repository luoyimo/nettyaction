package com.netty.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author hu
 * @Description: 配置文件处理  可扩展继承公共配置,自定义配置覆盖公共配置
 * @Date Create In 14:33 2018/10/30 0030
 */
public class PropertiesUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties prop;

    private static synchronized Properties loadProperties() {
        if (prop != null) {
            return prop;
        }
        prop = new Properties();
        InputStream in = null;
        try {
            //读取属性文件
            in = PropertiesUtil.class.getClassLoader().getResourceAsStream(propertiesName());
            ///加载属性列表
            prop.load(in);
        } catch (FileNotFoundException e) {
            logger.debug(e.getMessage());
        } catch (IOException e) {
            logger.debug(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.debug(e.getMessage());
                }
            }
        }
        return prop;
    }

    public static String getProperty(String name) {
        return loadProperties().getProperty(name);
    }


    private static String propertiesName() {
        String env;
        String active = System.getProperty("active");
        if (Objects.isNull(active)) {
            env = "dev.properties";
            logger.warn("没有自定义配置，使用默认配置");
        } else {
            env = active + ".properties";
        }
        return env;
    }
}
