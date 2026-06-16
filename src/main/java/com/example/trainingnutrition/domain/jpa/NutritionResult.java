package com.example.trainingnutrition.domain.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nutrition_results", indexes = {
        @Index(name = "idx_nutrition_results_correlation_id", columnList = "correlation_id"),
        @Index(name = "idx_nutrition_results_user_id", columnList = "user_id")
})
@Getter
@Setter
public class NutritionResult {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "correlation_id", nullable = false, unique = true, length = 100)
    private String correlationId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "calories", nullable = false)
    private Double calories;

    @Column(name = "proteins", nullable = false)
    private Double proteins;

    @Column(name = "fats", nullable = false)
    private Double fats;

    @Column(name = "carbohydrates", nullable = false)
    private Double carbohydrates;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    public void prePersist() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}
