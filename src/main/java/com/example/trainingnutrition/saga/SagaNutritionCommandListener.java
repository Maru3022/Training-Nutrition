package com.example.trainingnutrition.saga;

import com.example.trainingnutrition.domain.jpa.UserNutritionProfile;
import com.example.trainingnutrition.outbox.OutboxEvent;
import com.example.trainingnutrition.outbox.OutboxEventRepository;
import com.example.trainingnutrition.repository.jpa.UserNutritionProfileRepository;
import com.example.trainingnutrition.service.nutrition.KbjuCalculatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaNutritionCommandListener {

    private static final String STEP = "NUTRITION";

    private final UserNutritionProfileRepository profileRepository;
    private final KbjuCalculatorService kbjuCalculatorService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "saga-nutrition-command", containerFactory = "sagaKafkaListenerContainerFactory",
            groupId = "nutrition-service-saga")
    @Transactional
    public void onCommand(SagaCommandEvent event) {
        log.info("Received saga-nutrition-command: sagaId={}, status={}", event.getSagaId(), event.getStatus());

        if ("ROLLBACK".equals(event.getStatus())) {
            handleRollback(event);
            return;
        }

        if (!"EXECUTE".equals(event.getStatus())) {
            return;
        }

        Map<String, Object> data = event.getData();
        String userId = data != null ? String.valueOf(data.get("userId")) : null;
        if (userId == null || "null".equals(userId)) {
            publishResponse(event, "FAILED", Map.of("reason", "userId missing in payload"));
            return;
        }

        UserNutritionProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> profileRepository.save(kbjuCalculatorService.buildDefaultProfile(userId)));

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("profileId", profile.getId());
        responseData.put("dailyCalories", profile.getDailyCalories());
        responseData.put("proteinG", profile.getProteinG());
        responseData.put("fatG", profile.getFatG());
        responseData.put("carbsG", profile.getCarbsG());

        publishResponse(event, "SUCCESS", responseData);
    }

    private void handleRollback(SagaCommandEvent event) {
        Map<String, Object> data = event.getData();
        Object profileIdRaw = data != null ? data.get("profileId") : null;
        if (profileIdRaw != null) {
            try {
                Long profileId = Long.valueOf(profileIdRaw.toString());
                profileRepository.deleteById(profileId);
                log.info("Nutrition profile {} deleted as compensation for saga {}", profileId, event.getSagaId());
            } catch (Exception e) {
                log.warn("Could not delete nutrition profile for rollback of saga {}: {}", event.getSagaId(), e.getMessage());
            }
        }
        publishResponse(event, "ROLLBACK_DONE", null);
    }

    private void publishResponse(SagaCommandEvent command, String status, Map<String, Object> data) {
        try {
            SagaResponseEvent response = new SagaResponseEvent();
            response.setEventId(UUID.randomUUID().toString());
            response.setSagaId(command.getSagaId());
            response.setStep(STEP);
            response.setStatus(status);
            response.setData(data);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setTopic("saga-nutrition-response");
            outboxEvent.setKey(command.getSagaId());
            outboxEvent.setPayload(objectMapper.writeValueAsString(response));
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to publish saga-nutrition-response: {}", e.getMessage(), e);
        }
    }
}