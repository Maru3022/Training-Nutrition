package com.example.trainingnutrition.Repository;

import com.example.trainingnutrition.Domain.NutritionTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionTipRepository extends JpaRepository<NutritionTip,Long> {
}
