package com.example.trainingnutrition.domain.elastic;

import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "nutrition_tips")
public class NutritionTipDocument {

    @Id
    private String id; // ИЗМЕНИ НА STRING

    @Field(type = FieldType.Text, name = "title")
    private String title;

    @Field(type = FieldType.Text, name = "content")
    private String content;

    @Field(type = FieldType.Keyword, name = "category")
    private String category;
}