package com.petshop.functions.pets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.petshop.functions.pets",
    "com.petshop.functions.shared"
})
@EntityScan(basePackages = "com.petshop.functions.shared.model")
@EnableJpaRepositories(basePackages = "com.petshop.functions.shared.repository")
public class PetsFunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetsFunctionApplication.class, args);
    }
}
