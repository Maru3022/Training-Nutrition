package com.example.trainingnutrition.service.tracking;

import com.example.trainingnutrition.service.messaging.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {
    private final KafkaProducerService  kafkaProducerService;

    public void logWorkout(
            String userId,
            String exerciseType,
            int durationMinutes
    ){
        log.info("Logging workout for user {}: {} for {} min", userId, exerciseType, durationMinutes);

        String message = String.format("User %s finished %d min of %s", userId, durationMinutes, exerciseType);
        kafkaProducerService.sendMessage("training-topic", message);
    }
}
