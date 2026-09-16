package edu.eci.networkinglab.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathSanitizerTest {

    @Test
    void rootMapsToEmptyString() {
        assertEquals("", PathSanitizer.sanitize("/"));
    }

    @Test
    void normalPathIsPreserved() {
        assertEquals("images/logo.png", PathSanitizer.sanitize("/images/logo.png"));
    }

    @Test
    void decodesUrlEncodedSegments() {
        assertEquals("my file.png", PathSanitizer.sanitize("/my%20file.png"));
    }

    @Test
    void collapsesInnocuousDotDot() {
        // "a/../images/logo.png" never leaves the root: it's equivalent to "images/logo.png".
        assertEquals("images/logo.png", PathSanitizer.sanitize("/a/../images/logo.png"));
    }

    @Test
    void rejectsTraversalAboveRoot() {
        assertThrows(PathSanitizer.UnsafePathException.class,
                () -> PathSanitizer.sanitize("/../../etc/passwd"));
    }

    @Test
    void rejectsEncodedTraversalAboveRoot() {
        assertThrows(PathSanitizer.UnsafePathException.class,
                () -> PathSanitizer.sanitize("/images/../../etc/passwd"));
    }
}
