package com.example.trainingnutrition.Service.nutrition.impl;

import com.example.trainingnutrition.Domain.elastic.NutritionTipEntity;
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
    private final NutritionTipRepository jpaRepository;
    private final KafkaProducerService kafkaProducer;

    public NutritionTipEntity createTip(NutritionTipEntity tip){
        NutritionTipEntity saved = jpaRepository.save(tip);
        kafkaProducer.sendMessage("nutrition-topic", saved);
        return saved;
    }

    @Override
    @Cacheable(value = "tips")
    public List<NutritionTipEntity> getAllTips(){
        return jpaRepository.findAll();
    }

    @Override
    public NutritionTipEntity getTipById(Long id){
        return jpaRepository.findById(id)
                .orElseThrow(
                () -> new RuntimeException("Tip not found: " + id)
        );
    }
}