package edu.eci.networkinglab.server.services;

import edu.eci.networkinglab.server.Json;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * The four hardcoded services this lab exposes. Deliberately NOT a router
 * or framework: {@link edu.eci.networkinglab.server.HttpServerApp} matches
 * each URL with an explicit if/else and calls straight into these methods.
 * No per-request state is kept anywhere in this class.
 */
public final class Services {

    private static final int MAX_SIMULATED_DELAY_MS = 15_000;

    private Services() {
    }

    /** GET /api/greet?name=... */
    public static String greeting(Map<String, String> query) {
        String name = query.get("name");
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Query parameter 'name' is required and cannot be blank.");
        }
        String safeName = Json.escape(name.trim());
        return "{"
                + "\"service\":\"greet\","
                + "\"name\":" + Json.quoted(name.trim()) + ","
                + "\"message\":\"Hello, " + safeName + "!\""
                + "}";
    }

    /** GET /api/square?value=... */
    public static String square(Map<String, String> query) {
        String raw = query.get("value");
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Query parameter 'value' is required and cannot be blank.");
        }
        double value;
        try {
            value = Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Query parameter 'value' must be a valid number, got: " + raw);
        }
        if (!Double.isFinite(value)) {
            throw new BadRequestException("Query parameter 'value' must be a finite number.");
        }
        double result = value * value;
        return "{"
                + "\"service\":\"square\","
                + "\"input\":" + formatNumber(value) + ","
                + "\"result\":" + formatNumber(result)
                + "}";
    }

    /**
     * GET /api/time[?delayMs=...]
     * The optional delayMs parameter (capped) exists only to let section 6.2
     * of the lab demonstrate the sequential bottleneck on demand.
     */
    public static String serverTime(Map<String, String> query) {
        String delayRaw = query.get("delayMs");
        if (delayRaw != null && !delayRaw.isBlank()) {
            int delayMs;
            try {
                delayMs = Integer.parseInt(delayRaw.trim());
            } catch (NumberFormatException e) {
                throw new BadRequestException("Query parameter 'delayMs' must be an integer, got: " + delayRaw);
            }
            if (delayMs < 0 || delayMs > MAX_SIMULATED_DELAY_MS) {
                throw new BadRequestException("Query parameter 'delayMs' must be between 0 and " + MAX_SIMULATED_DELAY_MS + ".");
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Instant now = Instant.now();
        return "{"
                + "\"service\":\"time\","
                + "\"serverTimeIso\":" + Json.quoted(DateTimeFormatter.ISO_INSTANT.format(now)) + ","
                + "\"epochMillis\":" + now.toEpochMilli()
                + "}";
    }

    /** GET /api/health */
    public static String health() {
        return "{\"service\":\"health\",\"status\":\"ok\"}";
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
