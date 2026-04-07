package com.example.trainingnutrition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//ToDo: Интеграционные и JUnit тесты

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(basePackages = "com.example.trainingnutrition.repository.jpa")
public class TrainingNutritionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainingNutritionApplication.class, args);
    }

    @Configuration
    @EnableElasticsearchRepositories(basePackages = "com.example.trainingnutrition.repository.elastic")
    @ConditionalOnExpression("!'${spring.elasticsearch.uris:}'.isEmpty()")
    static class ElasticsearchConfig {
        // This configuration is only active when Elasticsearch URIs are configured (non-empty)
    }
}