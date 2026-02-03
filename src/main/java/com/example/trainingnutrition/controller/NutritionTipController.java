package com.example.trainingnutrition.controller;

import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.service.nutrition.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tips")
@RequiredArgsConstructor
public class NutritionTipController {

    private final TipService tipService;

    @PostMapping
    public ResponseEntity<NutritionTipEntity> create(
            @RequestBody NutritionTipEntity tip
    ){
        return ResponseEntity.ok(tipService.createTip(tip));
    }

    @GetMapping
    public ResponseEntity<List<NutritionTipEntity>> getAll(){
        return ResponseEntity.ok(tipService.getAllTips());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NutritionTipEntity> getById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(tipService.getTipById(id));
    }

}
