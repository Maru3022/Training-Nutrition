package com.example.trainingnutrition.controller;

import com.example.trainingnutrition.domain.elastic.NutritionTipDocument;
import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.service.nutrition.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NutritionTipController {

    private final TipService tipService;

    @GetMapping
    public List<NutritionTipEntity> getAllTips() {
        return tipService.getAllTips();
    }

    @PostMapping
    public NutritionTipEntity createTip(@RequestBody NutritionTipEntity tip) {
        return tipService.save(tip);
    }

    @GetMapping("/search")
    public List<NutritionTipDocument> search(@RequestParam String term) {
        return tipService.searchTips(term);
    }
}