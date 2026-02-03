package com.example.trainingnutrition.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dietary_recommendation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietaryRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String goal;
    private Integer recommendedCalories;
    private Double proteinGrams;
    private Double fatGrams;
    private Double carbsGrams;

    @Column(length = 1000)
    private String description;
}
