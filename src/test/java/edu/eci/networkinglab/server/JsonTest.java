package edu.eci.networkinglab.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonTest {

    @Test
    void escapesQuotesAndBackslashes() {
        assertEquals("a\\\"b\\\\c", Json.escape("a\"b\\c"));
    }

    @Test
    void escapesNewlinesAndTabs() {
        assertEquals("a\\nb\\tc", Json.escape("a\nb\tc"));
    }

    @Test
    void escapesControlCharacters() {
        String controlChar = Character.toString((char) 1);
        assertEquals("\\u0001", Json.escape(controlChar));
    }

    @Test
    void quotedWrapsInDoubleQuotes() {
        assertEquals("\"hello\"", Json.quoted("hello"));
    }

    @Test
    void preventsJsonInjectionViaUntrustedInput() {
        // An attacker-controlled value cannot break out of its JSON string context.
        String malicious = "\", \"admin\": true, \"x\": \"";
        String quoted = Json.quoted(malicious);
        assertEquals("\"\\\", \\\"admin\\\": true, \\\"x\\\": \\\"\"", quoted);
    }
}
