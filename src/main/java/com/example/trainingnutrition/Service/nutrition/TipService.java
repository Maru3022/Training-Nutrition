package com.example.trainingnutrition.Service.nutrition;

import com.example.trainingnutrition.Domain.NutritionTip;

import java.util.List;

public interface TipService {
    NutritionTip createTip(NutritionTip tip);
    List<NutritionTip> getAllTips();
    NutritionTip getTipById(Long id);
}
