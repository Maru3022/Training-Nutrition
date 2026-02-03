package com.example.trainingnutrition.service.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "nutrition-topic")
    public void consume(Object message) {
        System.out.println("Received a message from kafka: " + message);
    }

}
