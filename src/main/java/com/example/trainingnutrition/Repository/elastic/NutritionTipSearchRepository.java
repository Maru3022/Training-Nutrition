package com.example.trainingnutrition.Repository.elastic;

import com.example.trainingnutrition.Domain.NutritionTip;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionTipSearchRepository extends ElasticsearchRepository<NutritionTip,Long> {
}
