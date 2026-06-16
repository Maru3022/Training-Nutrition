package com.example.trainingnutrition.service.nutrition;

import com.example.trainingnutrition.dto.NutritionDTO;
import org.springframework.stereotype.Service;

@Service
public class NutritionCalculationService {

    private static final double DEFAULT_ACTIVITY_LEVEL = 1.55;
    private static final double PROTEIN_MULTIPLIER = 1.8;
    private static final double FAT_CALORIE_RATIO = 0.25;
    private static final double KCAL_PER_GRAM_FAT = 9.0;
    private static final double KCAL_PER_GRAM_PROTEIN = 4.0;
    private static final double KCAL_PER_GRAM_CARBS = 4.0;

    public NutritionDTO calculate(double weight, double height, int age, Double activityLevel) {
        double effectiveActivityLevel = activityLevel != null ? activityLevel : DEFAULT_ACTIVITY_LEVEL;
        double bmr = calculateBmr(weight, height, age);
        double tdee = bmr * effectiveActivityLevel;

        double proteins = weight * PROTEIN_MULTIPLIER;
        double fats = (tdee * FAT_CALORIE_RATIO) / KCAL_PER_GRAM_FAT;
        double carbohydrates = (tdee - proteins * KCAL_PER_GRAM_PROTEIN - fats * KCAL_PER_GRAM_FAT) / KCAL_PER_GRAM_CARBS;

        return new NutritionDTO(
                roundOneDecimal(tdee),
                roundOneDecimal(proteins),
                roundOneDecimal(fats),
                roundOneDecimal(carbohydrates)
        );
    }

    private double calculateBmr(double weight, double height, int age) {
        return (10.0 * weight) + (6.25 * height) - (5.0 * age);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
