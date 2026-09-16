package edu.eci.networkinglab.server.services;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicesTest {

    @Test
    void greetingReturnsMessageWithName() {
        String json = Services.greeting(Map.of("name", "Ada"));
        assertTrue(json.contains("\"name\":\"Ada\""));
        assertTrue(json.contains("Hello, Ada!"));
    }

    @Test
    void greetingRejectsMissingName() {
        assertThrows(BadRequestException.class, () -> Services.greeting(Map.of()));
    }

    @Test
    void greetingRejectsBlankName() {
        assertThrows(BadRequestException.class, () -> Services.greeting(Map.of("name", "   ")));
    }

    @Test
    void greetingEscapesNameInMessage() {
        String json = Services.greeting(Map.of("name", "A\"B"));
        assertTrue(json.contains("\\\"B"));
    }

    @Test
    void squareComputesIntegerResult() {
        String json = Services.square(Map.of("value", "5"));
        assertTrue(json.contains("\"input\":5"));
        assertTrue(json.contains("\"result\":25"));
    }

    @Test
    void squareComputesDecimalResult() {
        String json = Services.square(Map.of("value", "2.5"));
        assertTrue(json.contains("\"input\":2.5"));
        assertTrue(json.contains("\"result\":6.25"));
    }

    @Test
    void squareRejectsMissingValue() {
        assertThrows(BadRequestException.class, () -> Services.square(Map.of()));
    }

    @Test
    void squareRejectsNonNumericValue() {
        assertThrows(BadRequestException.class, () -> Services.square(Map.of("value", "not-a-number")));
    }

    @Test
    void serverTimeReturnsIsoTimestamp() {
        String json = Services.serverTime(Map.of());
        assertTrue(json.contains("\"serverTimeIso\":"));
        assertTrue(json.contains("\"epochMillis\":"));
    }

    @Test
    void serverTimeRejectsExcessiveDelay() {
        assertThrows(BadRequestException.class, () -> Services.serverTime(Map.of("delayMs", "999999")));
    }

    @Test
    void serverTimeRejectsNonNumericDelay() {
        assertThrows(BadRequestException.class, () -> Services.serverTime(Map.of("delayMs", "soon")));
    }

    @Test
    void healthReturnsOk() {
        String json = Services.health();
        assertTrue(json.contains("\"status\":\"ok\""));
    }
}
