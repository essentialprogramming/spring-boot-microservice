package com.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.config.Log4JConfig.configureLog4j;

/**
 * Entry point for SpringBootApplication
 */
@SpringBootApplication(scanBasePackages = {"com.*"})
public class Server {

    public static void main(String[] args) {

        configureLog4j();
        SpringApplication.run(Server.class, args);
    }
}
