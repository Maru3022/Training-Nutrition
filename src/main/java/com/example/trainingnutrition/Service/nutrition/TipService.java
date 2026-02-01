package com.example.trainingnutrition.Service.nutrition;

import com.example.trainingnutrition.Domain.jpa.NutritionTipEntity;

import java.util.List;

public interface TipService {
    NutritionTipEntity createTip(NutritionTipEntity tip);
    List<NutritionTipEntity> getAllTips();
    NutritionTipEntity getTipById(Long id);
}
