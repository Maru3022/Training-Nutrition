package com.example.trainingnutrition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {
        "com.example.trainingnutrition.repository.jpa",
        "com.example.trainingnutrition.outbox"
})
public class TrainingNutritionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainingNutritionApplication.class, args);
    }
}