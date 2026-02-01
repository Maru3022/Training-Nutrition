package com.example.trainingnutrition.Controller;

import com.example.trainingnutrition.Domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.Service.nutrition.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tips")
@RequiredArgsConstructor
public class AdminTipController {

    private final TipService tipService;

    @PostMapping
    public ResponseEntity<NutritionTipEntity> create(
            @RequestBody NutritionTipEntity nutritionTipEntity
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(tipService.createTip(nutritionTipEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ){
        return ResponseEntity.noContent().build();
    }
}
