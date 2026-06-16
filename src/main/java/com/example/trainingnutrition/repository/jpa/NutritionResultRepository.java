package com.example.trainingnutrition.repository.jpa;

import com.example.trainingnutrition.domain.jpa.NutritionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NutritionResultRepository extends JpaRepository<NutritionResult, java.util.UUID> {
    boolean existsByCorrelationId(String correlationId);
    Optional<NutritionResult> findByCorrelationId(String correlationId);
    List<NutritionResult> findByUserId(String userId);
}
