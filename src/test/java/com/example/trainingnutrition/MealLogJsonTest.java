package com.example.trainingnutrition;

import com.example.trainingnutrition.domain.jpa.MealLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MealLogJsonTest {
    @Test
    void clientCannotChooseExistingRecordId() throws Exception {
        var mapper = new ObjectMapper();
        var log = mapper.readValue("{\"id\":123,\"foodName\":\"Apple\"}", MealLog.class);
        assertNull(log.getId());
        log.setId(456L);
        assertEquals(456L, mapper.readTree(mapper.writeValueAsString(log)).get("id").asLong());
    }
}
