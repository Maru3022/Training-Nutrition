package com.example.trainingnutrition.controller;

import com.example.trainingnutrition.dto.AdviceResponse;
import com.example.trainingnutrition.service.nutrition.NutritionAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/advice")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdviceController {

    private final NutritionAdvisor nutritionAdvisor;

    @GetMapping("/{userId}")
    public AdviceResponse getDailyAdvice(@PathVariable String userId) {
        return new AdviceResponse(userId, nutritionAdvisor.generateDailyAdvice(userId));
    }
}
