package com.example.trainingnutrition.service.nutrition;

import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;

import java.util.List;

public interface TipService {
    NutritionTipEntity createTip(NutritionTipEntity tip);
    List<NutritionTipEntity> getAllTips();
    NutritionTipEntity getTipById(Long id);
}
