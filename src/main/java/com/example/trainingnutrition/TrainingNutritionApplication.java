package com.example.trainingnutrition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//ToDo: Интеграционные и JUnit тесты

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(basePackages = "com.example.trainingnutrition.repository.jpa")
public class TrainingNutritionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainingNutritionApplication.class, args);
    }
}