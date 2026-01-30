package com.example.trainingnutrition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(basePackages = "com.example.trainingnutrition.Repository.jpa")
@EnableElasticsearchRepositories(basePackages = "com.example.trainingnutrition.Repository.elastic")
public class TrainingNutritionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainingNutritionApplication.class, args);
    }
}