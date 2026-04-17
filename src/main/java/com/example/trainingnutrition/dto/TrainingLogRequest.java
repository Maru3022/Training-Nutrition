package com.example.trainingnutrition.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TrainingLogRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String exerciseType;

    @Min(1)
    private int durationMinutes;
}
