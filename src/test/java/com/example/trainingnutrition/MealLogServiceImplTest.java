package com.example.trainingnutrition;

import com.example.trainingnutrition.domain.jpa.MealLog;
import com.example.trainingnutrition.repository.jpa.MealLogRepository;
import com.example.trainingnutrition.service.nutrition.impl.MealLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MealLogServiceImplTest {

    @Mock
    private MealLogRepository mealLogRepository;

    @InjectMocks
    private MealLogServiceImpl mealLogService;

    @Test
    void saveLog_ShouldSetTimestamp(){
        MealLog log = new MealLog();
        log.setFoodName("Apple");
        when(mealLogRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        MealLog saved = mealLogService.saveLog(log);
        assertNotNull(saved.getConsumedAt());
        verify(mealLogRepository).save(log);
    }
}