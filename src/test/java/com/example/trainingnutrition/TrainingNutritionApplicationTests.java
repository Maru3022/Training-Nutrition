package com.example.trainingnutrition;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.trainingnutrition.repository.elastic.NutritionTipSearchRepository;
import com.example.trainingnutrition.service.messaging.KafkaConsumerService;
import com.example.trainingnutrition.service.messaging.KafkaProducerService;
import com.example.trainingnutrition.service.notification.NotificationService;

@SpringBootTest
@ActiveProfiles("test")
@EnableJpaRepositories(basePackages = "com.example.trainingnutrition.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "com.example.trainingnutrition.repository.elastic")
@EntityScan(basePackages = "com.example.trainingnutrition")
class TrainingNutritionApplicationTests {

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private KafkaConsumerService kafkaConsumerService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NutritionTipSearchRepository nutritionTipSearchRepository;

    @Test
    void contextLoads() {
    }
}