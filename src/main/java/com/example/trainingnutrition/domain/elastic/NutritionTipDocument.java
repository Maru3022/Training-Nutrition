package com.example.trainingnutrition.domain.elastic;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "nutrition_tips")
public class NutritionTipDocument {

    @Id
    private Long id;
    private String title;
    private String content;
    private String category;
}
