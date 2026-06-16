package com.example.trainingnutrition.saga;

import lombok.Data;

@Data
public class NutritionResponseEvent {
    private String correlationId;
    private String userId;
    private Double calories;
    private Double proteins;
    private Double fats;
    private Double carbohydrates;
    private Boolean success;
    private String errorMessage;
}
