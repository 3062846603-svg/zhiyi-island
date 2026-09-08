package com.example.zhiyiislandbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.zhiyiislandbackend.mapper")
@EnableScheduling
@EnableAsync
public class ZhiyiIslandApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyiIslandApplication.class, args);
    }

}
