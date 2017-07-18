package com.stephen.lab;

import org.apache.log4j.PropertyConfigurator;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by stephen on 2017/7/14.
 */
@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    static {
        try {
            // 初始化log4j
            String log4jPath = "";
//            if(Constant.LOG_MODE == 1){
//                // 配置线上地址
//                log4jPath = App.class.getClassLoader().getResource("").getPath()+"pms-api-services/config/log4j.properties";
//                logger.info("Log4j线上生产模式初始化。。。");
//            }else{
            // 配置本地地址
            log4jPath = Application.class.getClassLoader().getResource("").getPath() + "log4j.properties";
//            logger.info("Log4j初始化... ...");
//            }
            logger.info("初始化Log4j......");
            logger.info("path is " + log4jPath);
            PropertyConfigurator.configure(log4jPath);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}