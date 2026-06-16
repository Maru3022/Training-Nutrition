package com.example.trainingnutrition.saga;

import com.example.trainingnutrition.domain.jpa.NutritionResult;
import com.example.trainingnutrition.dto.NutritionDTO;
import com.example.trainingnutrition.repository.jpa.NutritionResultRepository;
import com.example.trainingnutrition.service.messaging.KafkaProducerService;
import com.example.trainingnutrition.service.nutrition.NutritionCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionSagaConsumer {

    private final NutritionCalculationService calculationService;
    private final NutritionResultRepository nutritionResultRepository;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(topics = "saga.nutrition.calculate", containerFactory = "nutritionSagaKafkaListenerContainerFactory")
    @Transactional
    public void handleCalculate(ConsumerRecord<String, NutritionCalculateEvent> record,
                                 Acknowledgment ack) {
        NutritionCalculateEvent event = record.value();
        if (event == null || event.getCorrelationId() == null) {
            log.warn("Received invalid nutrition calculation event, skipping");
            ack.acknowledge();
            return;
        }

        String correlationId = event.getCorrelationId();
        String userId = event.getUserId();

        if (nutritionResultRepository.existsByCorrelationId(correlationId)) {
            log.info("Skipping already processed nutrition calculation: correlationId={}, userId={}", correlationId, userId);
            ack.acknowledge();
            return;
        }

        try {
            NutritionDTO result = calculationService.calculate(
                    event.getWeight(),
                    event.getHeight(),
                    event.getAge(),
                    event.getActivityLevel()
            );

            NutritionResult persisted = new NutritionResult();
            persisted.setCorrelationId(correlationId);
            persisted.setUserId(userId);
            persisted.setCalories(result.getCalories());
            persisted.setProteins(result.getProteins());
            persisted.setFats(result.getFats());
            persisted.setCarbohydrates(result.getCarbohydrates());
            nutritionResultRepository.save(persisted);

            log.info("Nutrition calculation completed for userId={}, correlationId={}", userId, correlationId);

            NutritionResponseEvent response = new NutritionResponseEvent();
            response.setCorrelationId(correlationId);
            response.setUserId(userId);
            response.setCalories(result.getCalories());
            response.setProteins(result.getProteins());
            response.setFats(result.getFats());
            response.setCarbohydrates(result.getCarbohydrates());
            response.setSuccess(true);
            kafkaProducerService.sendMessage("saga.nutrition.response", response);
        } catch (Exception ex) {
            log.error("Error processing nutrition calculation event: correlationId={}, userId={}, error={}", correlationId, userId, ex.getMessage(), ex);
            NutritionResponseEvent response = new NutritionResponseEvent();
            response.setCorrelationId(correlationId);
            response.setUserId(userId);
            response.setSuccess(false);
            response.setErrorMessage(ex.getMessage());
            kafkaProducerService.sendMessage("saga.nutrition.response", response);
        } finally {
            ack.acknowledge();
        }
    }
}
