package com.example.trainingnutrition.service.nutrition.impl;

import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.domain.elastic.NutritionTipDocument;
import com.example.trainingnutrition.repository.elastic.NutritionTipSearchRepository;
import com.example.trainingnutrition.repository.jpa.NutritionTipRepository;
import com.example.trainingnutrition.service.messaging.KafkaProducerService;
import com.example.trainingnutrition.service.nutrition.TipService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTipServiceImpl implements TipService {

    private final NutritionTipRepository jpaRepository;
    private final NutritionTipSearchRepository elasticRepository;
    private final KafkaProducerService kafkaProducer;

    @Override
    @Transactional
    @CacheEvict(value = "tips", allEntries = true)
    public NutritionTipEntity save(NutritionTipEntity tip) {
        log.debug("Saving tip: {}", tip.getTitle());
        // 1. Сохраняем в SQL (чтобы увидеть в Swagger и Dashboard)
        NutritionTipEntity savedTip = jpaRepository.save(tip);

        NutritionTipDocument doc = new NutritionTipDocument();
        doc.setId(savedTip.getId().toString());
        doc.setTitle(savedTip.getTitle());
        doc.setContent(savedTip.getContent());
        doc.setCategory(savedTip.getCategory());

        elasticRepository.save(doc);

        kafkaProducer.sendMessage("new-tip", "Добавлен новый совет: " + savedTip.getTitle());

        return savedTip;
    }

    @Override
    @Cacheable(value = "tips")
    public List<NutritionTipEntity> getAllTips() {
        return jpaRepository.findAll();
    }

    @Override
    public NutritionTipEntity getTipById(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tip not found: " + id));
    }

    @Override
    public List<NutritionTipDocument> searchTips(String term) {
        // Вызываем метод, который мы добавили в NutritionTipSearchRepository
        return elasticRepository.findByTitleContainingOrContentContaining(term, term);
    }
}