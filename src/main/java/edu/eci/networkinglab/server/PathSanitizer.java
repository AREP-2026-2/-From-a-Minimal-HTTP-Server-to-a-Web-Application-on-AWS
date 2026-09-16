package edu.eci.networkinglab.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Normalizes a requested URL path into a safe, relative path inside the
 * public-resources area. Rejects any attempt to escape that area (e.g. via
 * {@code ../..} segments, raw or URL-encoded).
 */
public final class PathSanitizer {

    private PathSanitizer() {
    }

    public static final class UnsafePathException extends RuntimeException {
        public UnsafePathException(String message) {
            super(message);
        }
    }

    /**
     * @param rawPath the path portion of the request line (no query string),
     *                e.g. {@code "/images/logo.png"} or {@code "/"}.
     * @return a normalized, relative path with no leading slash and no
     *         {@code .} / {@code ..} segments, e.g. {@code "images/logo.png"}.
     *         An empty string means "the root" (mapped to index.html by the caller).
     * @throws UnsafePathException if the path tries to leave the public root.
     */
    public static String sanitize(String rawPath) {
        String decoded = decode(rawPath);

        Deque<String> stack = new ArrayDeque<>();
        for (String segment : decoded.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }
            if (segment.equals("..")) {
                if (stack.isEmpty()) {
                    throw new UnsafePathException("Path traversal attempt: " + rawPath);
                }
                stack.removeLast();
            } else {
                stack.addLast(segment);
            }
        }
        return String.join("/", stack);
    }

    private static String decode(String rawPath) {
        try {
            return URLDecoder.decode(rawPath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported; this branch is unreachable.
            throw new IllegalStateException(e);
        }
    }
}
