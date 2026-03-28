package com.payauth.payauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PayAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayAuthApplication.class, args);
    }

}
