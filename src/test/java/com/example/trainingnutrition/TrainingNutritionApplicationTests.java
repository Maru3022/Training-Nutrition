package com.example.trainingnutrition;

import com.example.trainingnutrition.repository.elastic.NutritionTipSearchRepository;
import com.example.trainingnutrition.service.messaging.KafkaConsumerService;
import com.example.trainingnutrition.service.messaging.KafkaProducerService;
import com.example.trainingnutrition.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class TrainingNutritionApplicationTests {

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private KafkaConsumerService kafkaConsumerService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NutritionTipSearchRepository nutritionTipSearchRepository;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void contextLoads() {
    }
}