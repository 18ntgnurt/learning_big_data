package com.dataengineering.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application for Transaction Stream Processor
 */
@SpringBootApplication
public class TransactionStreamApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TransactionStreamApplication.class, args);
    }
} 