package com.example.trainingnutrition.Service.nutrition.impl;

import com.example.trainingnutrition.Domain.jpa.MealLog;
import com.example.trainingnutrition.Service.nutrition.NutritionAdvisor;
import com.example.trainingnutrition.Service.nutrition.TipService;
import com.example.trainingnutrition.Service.tracking.MealLogService;
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

    public String generateDailyAdvice(
            String userId
    ) {
        List<MealLog> logs = mealLogService.getLogsByUserId(userId);

        int totalCalories = logs.stream()
                .mapToInt(MealLog::getCalories)
                .sum();

        log.debug("User {} total calories is {}", userId, totalCalories);

        String targetCategory = determineCategory(totalCalories);
        log.info("Selected category '{}' based on {} kcal", targetCategory, totalCalories);

        return tipService.getAllTips().stream()
                .filter((tip -> tip.getCategory().equalsIgnoreCase(targetCategory)))
                .map(tip -> String.format("Based on your intake (%d kcal): %s", totalCalories, tip.getContent()))
                .findAny().orElse("Keep maintaining a balanced diet and stay hydrated!");

    }

    private String determineCategory(int calories) {
        if (calories < 1500) return "BULKING";
        if (calories < 2500) return "CUTTING";
        return "MAINTENANCE";
    }
}