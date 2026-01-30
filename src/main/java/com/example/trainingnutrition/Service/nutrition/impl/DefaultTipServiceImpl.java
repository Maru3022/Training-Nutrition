package com.example.trainingnutrition.Service.nutrition.impl;

import com.example.trainingnutrition.Domain.NutritionTip;
import com.example.trainingnutrition.Repository.jpa.NutritionTipRepository;
import com.example.trainingnutrition.Service.messaging.KafkaProducerService;
import com.example.trainingnutrition.Service.nutrition.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultTipServiceImpl implements TipService {
    private final NutritionTipRepository repository;
    private final KafkaProducerService kafkaProducer;

    public NutritionTip createTip(NutritionTip tip){
        NutritionTip saved = repository.save(tip);
        kafkaProducer.sendMessage("nutrition-topic", saved);
        return saved;
    }

    @Override
    @Cacheable(value = "tips")
    public List<NutritionTip> getAllTips(){
        return repository.findAll();
    }

    @Override
    public NutritionTip getTipById(Long id){
        return repository.findById(id).orElseThrow();
    }
}