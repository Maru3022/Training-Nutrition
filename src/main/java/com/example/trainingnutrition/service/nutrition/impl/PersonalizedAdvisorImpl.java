package com.example.trainingnutrition.service.nutrition.impl;

import com.example.trainingnutrition.domain.jpa.MealLog;
import com.example.trainingnutrition.service.nutrition.NutritionAdvisor;
import com.example.trainingnutrition.service.nutrition.TipService;
import com.example.trainingnutrition.service.tracking.MealLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalizedAdvisorImpl implements NutritionAdvisor {

    private final MealLogService mealLogService;
    private final TipService tipService;

    public String generateDailyAdvice(String userId) {
        List<MealLog> logs = mealLogService.getLogsByUserId(userId);

        int totalCalories = logs.stream()
                .mapToInt(MealLog::getCalories)
                .sum();

        log.debug("User {} total calories is {}", userId, totalCalories);

        String targetCategory = determineCategory(totalCalories);
        log.info("Selected category '{}' based on {} kcal", targetCategory, totalCalories);

        return tipService.getAllTips().stream()
                .filter(tip -> tip.getCategory().equalsIgnoreCase(targetCategory))
                .map(tip -> String.format("Based on your intake (%d kcal): %s", totalCalories, tip.getContent()))
                .findAny()
                .orElse("Keep maintaining a balanced diet and stay hydrated!");
    }

    private String determineCategory(int calories) {
        if (calories < 1800) {
            return "CUTTING";
        }
        if (calories < 2600) {
            return "MAINTENANCE";
        }
        return "BULKING";
    }
}
