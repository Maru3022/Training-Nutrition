package com.example.trainingnutrition.Service.nutrition.impl;

import com.example.trainingnutrition.Domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.Domain.elastic.NutritionTipDocument;
import com.example.trainingnutrition.Repository.elastic.NutritionTipSearchRepository;
import com.example.trainingnutrition.Repository.jpa.NutritionTipRepository;
import com.example.trainingnutrition.Service.messaging.KafkaProducerService;
import com.example.trainingnutrition.Service.nutrition.TipService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultTipServiceImpl implements TipService {
    private final NutritionTipRepository jpaRepository;
    private final KafkaProducerService kafkaProducer;
    private final NutritionTipSearchRepository elasticRepository;

    @Override
    @Transactional
    @CacheEvict(value = "tips", allEntries = true)
    public NutritionTipEntity createTip(NutritionTipEntity tip){
        NutritionTipEntity saved = jpaRepository.save(tip);

        NutritionTipDocument doc =  new NutritionTipDocument();
        doc.setId(saved.getId());
        doc.setTitle(tip.getTitle());
        doc.setContent(tip.getContent());
        doc.setCategory(saved.getCategory());
        elasticRepository.save(doc);

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