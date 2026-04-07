package com.example.trainingnutrition.service.nutrition;

import com.example.trainingnutrition.domain.elastic.NutritionTipDocument;
import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import java.util.List;

public interface TipService {
    NutritionTipEntity save(NutritionTipEntity tip);
    List<NutritionTipEntity> getAllTips();
    NutritionTipEntity getTipById(Long id);
    List<NutritionTipDocument> searchTips(String term);
    void deleteById(Long id);
}