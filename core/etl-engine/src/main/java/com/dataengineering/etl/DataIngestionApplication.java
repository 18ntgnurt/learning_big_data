package com.dataengineering.etl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main Spring Boot application for Data Ingestion Service
 */
@SpringBootApplication
@EnableKafka
public class DataIngestionApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DataIngestionApplication.class, args);
    }
} 