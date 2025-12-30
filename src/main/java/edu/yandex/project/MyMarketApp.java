package edu.yandex.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MyMarketApp {

    public static void main(String[] args) {
        SpringApplication.run(MyMarketApp.class, args);
    }
}
