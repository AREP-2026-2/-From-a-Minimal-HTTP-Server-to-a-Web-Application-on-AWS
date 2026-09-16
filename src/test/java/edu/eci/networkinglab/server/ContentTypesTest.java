package edu.eci.networkinglab.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContentTypesTest {

    @Test
    void resolvesHtml() {
        assertEquals("text/html; charset=utf-8", ContentTypes.forPath("index.html"));
    }

    @Test
    void resolvesJavaScript() {
        assertEquals("application/javascript; charset=utf-8", ContentTypes.forPath("app.js"));
    }

    @Test
    void resolvesPng() {
        assertEquals("image/png", ContentTypes.forPath("images/logo.png"));
    }

    @Test
    void resolvesJpegBothExtensions() {
        assertEquals("image/jpeg", ContentTypes.forPath("images/banner.jpg"));
        assertEquals("image/jpeg", ContentTypes.forPath("images/banner.jpeg"));
    }

    @Test
    void unknownExtensionReturnsNull() {
        assertNull(ContentTypes.forPath("archive.zip"));
    }

    @Test
    void noExtensionReturnsNull() {
        assertNull(ContentTypes.forPath("Makefile"));
    }
}
