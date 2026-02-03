package com.example.trainingnutrition.service.tracking;

import com.example.trainingnutrition.domain.jpa.MealLog;

import java.util.List;

public interface MealLogService {
    MealLog saveLog(MealLog log);
    List<MealLog> getLogsByUserId(String userId);
    List<MealLog> findByUserId(String userId);
}
