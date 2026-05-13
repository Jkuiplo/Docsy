package com.google;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.UUID;

@SpringBootApplication
public class DmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(CompanyRepository repository) {
        return args -> {
            System.out.println("--- Сохраняем тестовую компанию ---");
            Company newCompany = new Company("Test Enterprise", UUID.randomUUID());
            repository.save(newCompany);
            System.out.println("--- Компания сохранена в БД! ---");
        };
    }
}