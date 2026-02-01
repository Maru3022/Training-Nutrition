package com.example.trainingnutrition.Repository.jpa;

import com.example.trainingnutrition.Domain.jpa.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog,Long> {
    List<MealLog> findByUserId(String userId);
}
