package com.example.trainingnutrition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.trainingnutrition.Repository")
@EnableElasticsearchRepositories(basePackages = "com.example.trainingnutrition.Repository")
public class TrainingNutritionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingNutritionApplication.class, args);
    }

}
