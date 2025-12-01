package com.petshop.functions.customers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Customers Function Application
 * Handles: CRUD for customers
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.petshop.functions.customers",
    "com.petshop.functions.shared"
})
@EntityScan(basePackages = "com.petshop.functions.shared.model")
@EnableJpaRepositories(basePackages = "com.petshop.functions.shared.repository")
public class CustomersFunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomersFunctionApplication.class, args);
    }
}
