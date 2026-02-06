package com.example.trainingnutrition.domain.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "JpaNutritionTip")
@Data
@Table(name = "nutrition_tips")
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String category;
}