package com.example.trainingnutrition.saga;

import lombok.Data;

@Data
public class NutritionCalculateEvent {
    private String correlationId;
    private String userId;
    private double weight;
    private double height;
    private int age;
    private Double activityLevel;
}
