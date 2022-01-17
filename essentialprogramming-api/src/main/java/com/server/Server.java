package com.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for SpringBootApplication
 */
@SpringBootApplication(scanBasePackages = {"com.*"})
public class Server {

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
