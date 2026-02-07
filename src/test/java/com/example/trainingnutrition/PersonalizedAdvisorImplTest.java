package com.example.trainingnutrition;

import com.example.trainingnutrition.domain.jpa.MealLog;
import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.service.nutrition.TipService;
import com.example.trainingnutrition.service.nutrition.impl.PersonalizedAdvisorImpl;
import com.example.trainingnutrition.service.tracking.MealLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalizedAdvisorImplTest {

    @Mock
    private MealLogService mealLogService;

    @Mock
    private TipService tipService;

    @InjectMocks
    private PersonalizedAdvisorImpl advisor;

    @Test
    void generateDailyAdvice_ShouldSelectCuttingCategory(){
        String userId = "user1";
        MealLog log = new MealLog();
        log.setCalories(2000);

        NutritionTipEntity tip = new NutritionTipEntity(1L,"Title","Less sugar","CUTTING");

        when(mealLogService.getLogsByUserId(userId)).thenReturn(List.of(log));
        when(tipService.getAllTips()).thenReturn(List.of(tip));

        String advice = advisor.generateDailyAdvice(userId);

        assertTrue(advice.contains("Less sugar"));
        assertTrue(advice.contains("2000 kcal"));
    }
}