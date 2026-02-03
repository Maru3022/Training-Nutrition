package com.example.trainingnutrition.repository.elastic;

import com.example.trainingnutrition.domain.elastic.NutritionTipDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionTipSearchRepository
        extends ElasticsearchRepository<NutritionTipDocument,Long> {
}
