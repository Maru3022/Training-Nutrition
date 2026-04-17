package com.example.trainingnutrition.controller;

import com.example.trainingnutrition.domain.jpa.MealLog;
import com.example.trainingnutrition.service.tracking.MealLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meal-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MealLogController {

    private final MealLogService mealLogService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealLog createMealLog(@RequestBody MealLog mealLog) {
        return mealLogService.saveLog(mealLog);
    }

    @GetMapping("/{userId}")
    public List<MealLog> getLogsByUserId(@PathVariable String userId) {
        return mealLogService.getLogsByUserId(userId);
    }
}
