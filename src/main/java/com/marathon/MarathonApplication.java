package com.marathon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarathonApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarathonApplication.class, args);
    }
}