package com.example.trainingnutrition.Service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @KafkaListener(topics = "nutrition-topic", groupId = "notification-group")
    public void handleNutritionEvent(
            Object message
    ) {
        log.info("Notification Service received message: {}", message);
        sendPushNotification("New Nutrition Tip!", message.toString());
    }

    private void sendPushNotification(
            String title,
            String body
    ) {
        log.info("Push notification sent: {} - {}", title, body);
    }
}
