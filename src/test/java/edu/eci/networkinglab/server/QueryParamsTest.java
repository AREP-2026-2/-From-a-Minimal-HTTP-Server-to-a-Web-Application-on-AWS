package edu.eci.networkinglab.server;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryParamsTest {

    @Test
    void emptyQueryReturnsEmptyMap() {
        assertTrue(QueryParams.parse("").isEmpty());
        assertTrue(QueryParams.parse(null).isEmpty());
    }

    @Test
    void parsesSingleParam() {
        Map<String, String> result = QueryParams.parse("name=Ada");
        assertEquals("Ada", result.get("name"));
    }

    @Test
    void parsesMultipleParams() {
        Map<String, String> result = QueryParams.parse("a=1&b=2");
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
    }

    @Test
    void decodesUrlEncodedValues() {
        Map<String, String> result = QueryParams.parse("name=Ada%20Lovelace%20%26%20Co");
        assertEquals("Ada Lovelace & Co", result.get("name"));
    }

    @Test
    void keyWithoutEqualsSignBecomesEmptyValue() {
        Map<String, String> result = QueryParams.parse("flag");
        assertEquals("", result.get("flag"));
    }
}
