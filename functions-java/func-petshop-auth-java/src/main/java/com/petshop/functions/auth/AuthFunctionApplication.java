package com.petshop.functions.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auth Function Application
 * Handles: login, register, validate token
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.petshop.functions.auth",
    "com.petshop.functions.shared"
})
@EntityScan(basePackages = "com.petshop.functions.shared.model")
@EnableJpaRepositories(basePackages = "com.petshop.functions.shared.repository")
public class AuthFunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthFunctionApplication.class, args);
    }
}
