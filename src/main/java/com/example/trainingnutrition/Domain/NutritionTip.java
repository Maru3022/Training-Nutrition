package com.example.trainingnutrition.Domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Entity
@Data
@Document(indexName = "nutrition_tips")
public class NutritionTip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String category;
}