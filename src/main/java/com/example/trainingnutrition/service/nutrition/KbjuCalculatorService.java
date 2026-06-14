package com.example.trainingnutrition.service.nutrition;

import com.example.trainingnutrition.domain.jpa.UserNutritionProfile;
import org.springframework.stereotype.Service;

@Service
public class KbjuCalculatorService {

    private static final int DEFAULT_CALORIES = 2200;
    private static final double PROTEIN_RATIO = 0.30; // 30% калорий из белка
    private static final double FAT_RATIO = 0.25;     // 25% калорий из жиров
    private static final double CARBS_RATIO = 0.45;   // 45% калорий из углеводов

    private static final double KCAL_PER_G_PROTEIN = 4.0;
    private static final double KCAL_PER_G_FAT = 9.0;
    private static final double KCAL_PER_G_CARBS = 4.0;

    public UserNutritionProfile buildDefaultProfile(String userId) {
        UserNutritionProfile profile = new UserNutritionProfile();
        profile.setUserId(userId);
        profile.setDailyCalories(DEFAULT_CALORIES);
        profile.setProteinG(round(DEFAULT_CALORIES * PROTEIN_RATIO / KCAL_PER_G_PROTEIN));
        profile.setFatG(round(DEFAULT_CALORIES * FAT_RATIO / KCAL_PER_G_FAT));
        profile.setCarbsG(round(DEFAULT_CALORIES * CARBS_RATIO / KCAL_PER_G_CARBS));
        profile.setAdvice("Начните с " + DEFAULT_CALORIES + " ккал/день: "
                + profile.getProteinG() + " г белка, "
                + profile.getFatG() + " г жиров, "
                + profile.getCarbsG() + " г углеводов. "
                + "Пейте воду, ешьте 3-4 раза в день и не пропускайте белок при каждом приёме пищи.");
        return profile;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}