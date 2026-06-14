package com.example.trainingnutrition.repository.jpa;

import com.example.trainingnutrition.domain.jpa.UserNutritionProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNutritionProfileRepository extends JpaRepository<UserNutritionProfile, Long> {
    Optional<UserNutritionProfile> findByUserId(String userId);
}