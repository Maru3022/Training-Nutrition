package com.example.trainingnutrition.DTO;

import lombok.Data;

import java.util.List;

@Data
public class UserPreferenceDTO {
    private String userId;
    private String dietType;
    private List<String> allergies;
    private Integer dailyCalorieTarget;
}
