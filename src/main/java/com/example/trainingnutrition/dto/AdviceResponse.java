package com.example.trainingnutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdviceResponse {
    private String userId;
    private String advice;
}
