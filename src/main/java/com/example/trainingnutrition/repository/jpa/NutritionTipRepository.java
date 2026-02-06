package com.example.trainingnutrition.repository.jpa;

import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionTipRepository
        extends JpaRepository<NutritionTipEntity,Long> {
}