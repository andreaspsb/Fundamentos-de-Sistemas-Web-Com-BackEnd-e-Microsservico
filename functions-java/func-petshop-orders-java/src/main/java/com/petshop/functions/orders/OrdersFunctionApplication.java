package com.petshop.functions.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.petshop.functions.orders",
    "com.petshop.functions.shared"
})
@EntityScan(basePackages = "com.petshop.functions.shared.model")
@EnableJpaRepositories(basePackages = "com.petshop.functions.shared.repository")
public class OrdersFunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersFunctionApplication.class, args);
    }
}
