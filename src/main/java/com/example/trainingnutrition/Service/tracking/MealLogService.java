package com.example.trainingnutrition.Service.tracking;

import com.example.trainingnutrition.Domain.jpa.MealLog;

import java.util.List;

public interface MealLogService {
    MealLog saveLog(MealLog log);
    List<MealLog> findByUserId(String userId);
}
