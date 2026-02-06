package com.example.trainingnutrition;

import com.example.trainingnutrition.domain.jpa.NutritionTipEntity;
import com.example.trainingnutrition.repository.elastic.NutritionTipSearchRepository;
import com.example.trainingnutrition.repository.jpa.NutritionTipRepository;
import com.example.trainingnutrition.service.messaging.KafkaProducerService;
import com.example.trainingnutrition.service.nutrition.impl.DefaultTipServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultTipServiceImplTest {

    @Mock
    private NutritionTipRepository jpaRepository;

    @Mock
    private NutritionTipSearchRepository elasticRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private DefaultTipServiceImpl tipService;

    @Test
    void save_ShouldSaveAllSystems(){
        NutritionTipEntity tip = new NutritionTipEntity(null,"Healthy Fat","Eat Avocado", "MAINTENANCE");
        NutritionTipEntity savedEntity = new NutritionTipEntity(10L, "Healthy Fat", "Eat Avocado", "MAINTENANCE");

        when(jpaRepository.save(any(NutritionTipEntity.class))).thenReturn(savedEntity);

        NutritionTipEntity result = tipService.save(tip);

        assertNotNull(result.getId());
        verify(jpaRepository, times(1)).save(any());
        verify(elasticRepository, times(1)).save(any());
        verify(kafkaProducerService, times(1)).sendMessage(eq("new-tip"), contains("Healthy Fat"));
    }
}