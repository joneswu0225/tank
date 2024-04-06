package com.jones.tank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
//@MapperScan("com.jones.marschat.mapper")
public class TankApplication {
    public static ConfigurableApplicationContext applicationContext;

    public static ConfigurableApplicationContext getApplicationContext(){
        return applicationContext;
    }
    public static void main(String[] args) {
        applicationContext = SpringApplication.run(TankApplication.class, args);
        System.out.printf("123");
    }

}
