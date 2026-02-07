package com.example.trainingnutrition;

import com.example.trainingnutrition.service.messaging.KafkaProducerService;
import com.example.trainingnutrition.service.tracking.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void logWorkout_ShouldSendCorrectMessageToKafka(){
        String userId = "user_777";
        String exercise = "Push-ups";
        int duration = 15;
        String expectedMessage = "User user_777 finished 15 min of Push-ups";

        trainingService.logWorkout(userId,exercise,duration);
        verify(kafkaProducerService).sendMessage("training-topic",expectedMessage);

    }

}