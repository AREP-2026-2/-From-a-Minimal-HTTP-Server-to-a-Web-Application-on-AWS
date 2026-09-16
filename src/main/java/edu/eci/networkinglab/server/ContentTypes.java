package edu.eci.networkinglab.server;

import java.util.Locale;
import java.util.Map;

/**
 * Explicit, hardcoded extension-to-content-type table. No framework, no
 * auto-detection: the lab is about seeing the mapping, not hiding it.
 */
public final class ContentTypes {

    private static final Map<String, String> TYPES_BY_EXTENSION = Map.of(
            "html", "text/html; charset=utf-8",
            "htm", "text/html; charset=utf-8",
            "js", "application/javascript; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "ico", "image/x-icon",
            "txt", "text/plain; charset=utf-8"
    );

    private ContentTypes() {
    }

    /**
     * Returns the content type for a resource path based on its extension,
     * or {@code null} when the extension is unknown/unsupported.
     */
    public static String forPath(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) {
            return null;
        }
        String extension = path.substring(dot + 1).toLowerCase(Locale.ROOT);
        return TYPES_BY_EXTENSION.get(extension);
    }
}
