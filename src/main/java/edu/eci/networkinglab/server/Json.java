package edu.eci.networkinglab.server;

/**
 * Minimal, dependency-free JSON string escaping. No untrusted value is ever
 * concatenated into a JSON response without going through {@link #escape}.
 */
public final class Json {

    private Json() {
    }

    public static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    public static String quoted(String value) {
        return "\"" + escape(value) + "\"";
    }
}
