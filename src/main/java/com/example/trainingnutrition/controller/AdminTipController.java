package com.example.trainingnutrition.controller;

import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.service.nutrition.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/tips")
@RequiredArgsConstructor
public class AdminTipController {

    private final TipService tipService;

    @PostMapping
    public ResponseEntity<NutritionTipEntity> create(
            @RequestHeader("X-User-Role") String role,
            @RequestBody NutritionTipEntity nutritionTipEntity
    ){
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipService.save(nutritionTipEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ){
        requireAdmin(role);
        tipService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator role is required");
        }
    }
}
