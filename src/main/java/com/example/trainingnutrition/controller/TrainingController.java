package com.example.trainingnutrition.controller;

import com.example.trainingnutrition.dto.TrainingLogRequest;
import com.example.trainingnutrition.service.tracking.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping("/log")
    public ResponseEntity<Map<String, String>> logWorkout(@Valid @RequestBody TrainingLogRequest request) {
        trainingService.logWorkout(request.getUserId(), request.getExerciseType(), request.getDurationMinutes());
        return ResponseEntity.ok(Map.of("status", "accepted"));
    }
}
