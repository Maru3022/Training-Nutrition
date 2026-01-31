package com.example.trainingnutrition;

import com.example.trainingnutrition.Service.messaging.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TrainingNutritionApplicationTests {

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @Test
    void contextLoads() {
    }
}