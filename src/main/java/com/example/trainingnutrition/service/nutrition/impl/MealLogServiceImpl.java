package com.example.trainingnutrition.service.nutrition.impl;

import com.example.trainingnutrition.domain.jpa.MealLog;
import com.example.trainingnutrition.repository.jpa.MealLogRepository;
import com.example.trainingnutrition.service.tracking.MealLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealLogServiceImpl implements MealLogService {
    private final MealLogRepository mealLogRepository;

    @Override
    public MealLog saveLog(
            MealLog log
    ) {
        log.setConsumedAt(LocalDateTime.now());
        return mealLogRepository.save(log);
    }

    @Override
    public List<MealLog> findByUserId(
            String userId
    ) {
        return mealLogRepository.findByUserId(userId);
    }

    @Override
    public List<MealLog> getLogsByUserId(
            String userId
    ){
        log.info("Fetching logs for user: {}",  userId);
        return mealLogRepository.findByUserId(userId);
    }
}
