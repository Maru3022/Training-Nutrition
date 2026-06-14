package com.example.trainingnutrition.saga;

import lombok.Data;

import java.util.Map;

@Data
public class SagaResponseEvent {
    private String eventId;
    private String sagaId;
    private String step;
    private String status;
    private Map<String, Object> data;
}