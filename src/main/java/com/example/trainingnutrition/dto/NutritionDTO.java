package com.example.trainingnutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionDTO {
    private double calories;
    private double proteins;
    private double fats;
    private double carbohydrates;
}
