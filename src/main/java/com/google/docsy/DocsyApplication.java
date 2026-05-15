package com.google.docsy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocsyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocsyApplication.class, args);
        System.out.println("--- Docsy DMS Backend is Running ---");
    }
}