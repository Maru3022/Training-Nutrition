package com.example.trainingnutrition.repository.elastic;

import com.example.trainingnutrition.domain.elastic.NutritionTipDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionTipSearchRepository
        extends ElasticsearchRepository<NutritionTipDocument, String> {
    List<NutritionTipDocument> findByTitleContainingOrContentContaining(String title, String content);
}
