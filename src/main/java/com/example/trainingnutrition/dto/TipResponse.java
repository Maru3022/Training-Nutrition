package com.example.trainingnutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipResponse {
    private Long id;
    private String title;
    private String content;
    private String category;
}
