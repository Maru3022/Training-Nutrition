package com.example.trainingnutrition;

import com.example.trainingnutrition.Repository.NutritionTipSearchRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(
        basePackages = "com.example.trainingnutrition.Repository",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = NutritionTipSearchRepository.class
        )
)
@EnableElasticsearchRepositories(
        basePackages = "com.example.trainingnutrition.Repository",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = NutritionTipSearchRepository.class
        )
)
public class TrainingNutritionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingNutritionApplication.class, args);
    }

}
