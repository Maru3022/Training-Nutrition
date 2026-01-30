package com.example.trainingnutrition;

import com.example.trainingnutrition.Service.messaging.KafkaProducerService;
import com.example.trainingnutrition.Service.nutrition.impl.DefaultTipServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.data.elasticsearch.enabled=false",
        "spring.kafka.bootstrap-servers=dummy:9092"
})
class TrainingNutritionApplicationTests {

    @MockBean
    private com.example.trainingnutrition.Service.messaging.KafkaProducerService kafkaProducerService;

    @Test
    void contextLoads() {}
}


