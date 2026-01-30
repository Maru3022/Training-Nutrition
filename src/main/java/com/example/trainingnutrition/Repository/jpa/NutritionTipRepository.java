package com.example.trainingnutrition.Repository.jpa;

import com.example.trainingnutrition.Domain.elastic.NutritionTipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionTipRepository
        extends JpaRepository<NutritionTipEntity,Long> {
}
