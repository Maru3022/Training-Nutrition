package com.example.trainingnutrition.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "nutrition-topic")
    public void consume(Object message) {
        log.info("Received a message from kafka: {}", message);
    }

}
