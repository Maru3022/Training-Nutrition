package com.example.trainingnutrition;

import com.example.trainingnutrition.Service.messaging.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TrainingNutritionApplicationTests {

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @Test
    void contextLoads() {
    }
}
