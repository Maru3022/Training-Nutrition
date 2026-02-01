package com.example.trainingnutrition.Service.nutrition.impl;

import com.example.trainingnutrition.Domain.jpa.MealLog;
import com.example.trainingnutrition.Repository.jpa.MealLogRepository;
import com.example.trainingnutrition.Service.tracking.MealLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealLogServiceImpl implements MealLogService {
    private final MealLogRepository mealLogRepository;

    @Override
    public MealLog saveLog(MealLog log) {
        log.setConsumedAt(LocalDateTime.now());
        return mealLogRepository.save(log);
    }

    @Override
    public List<MealLog> findByUserId(String userId) {
        return mealLogRepository.findByUserId(userId);
    }
}
