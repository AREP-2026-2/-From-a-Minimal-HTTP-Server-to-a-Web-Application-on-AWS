package edu.eci.networkinglab.server;

import edu.eci.networkinglab.server.services.BadRequestException;
import edu.eci.networkinglab.server.services.Services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * A deliberately sequential, socket-based HTTP server.
 *
 * <p>It accepts one TCP connection at a time, fully handles the request,
 * closes the connection, and only then accepts the next one. There is no
 * thread pool and no concurrency anywhere in this class: that limitation is
 * the point of this lab (see the README "Known limitations" section).
 *
 * <p>Routing is intentionally hardcoded: four fixed service paths are
 * checked with explicit if/else, everything else is treated as a static
 * resource under {@code public/} on the classpath.
 */
public final class HttpServerApp {

    private static final int DEFAULT_PORT = 8080;
    private static final String PUBLIC_ROOT = "public";
    private static final String INDEX_RESOURCE = "index.html";

    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server listening on port " + port + " (all interfaces). Press Ctrl+C to stop.");

            while (true) {
                try (Socket client = serverSocket.accept()) {
                    handleConnection(client);
                } catch (IOException e) {
                    // A single bad/aborted connection must not bring the server down.
                    log("Connection error: " + e.getMessage());
                }
            }
        }
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort.trim());
        }
        return DEFAULT_PORT;
    }

    private static void handleConnection(Socket client) throws IOException {
        long start = System.nanoTime();
        String clientAddress = client.getRemoteSocketAddress().toString();
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();

        String requestLine = readLine(in);
        if (requestLine == null || requestLine.isBlank()) {
            // Client connected and closed without sending anything usable.
            return;
        }

        // Consume (and ignore) headers so the stream is read fully and cleanly.
        String headerLine;
        while ((headerLine = readLine(in)) != null && !headerLine.isEmpty()) {
            // Headers are not needed for this lab's routing; just drain them.
        }

        int status;
        try {
            status = route(requestLine, out);
        } catch (Exception e) {
            log("Unhandled error while serving '" + requestLine + "': " + e);
            HttpResponses.sendPlainText(out, 500, "Internal Server Error", "Internal server error.");
            status = 500;
        }

        double ms = (System.nanoTime() - start) / 1_000_000.0;
        log(String.format("%s \"%s\" -> %d (%.1f ms)", clientAddress, requestLine, status, ms));
    }

    /** @return the HTTP status code produced, for logging. */
    private static int route(String requestLine, OutputStream out) throws IOException {
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            HttpResponses.sendPlainText(out, 400, "Bad Request", "Malformed request line.");
            return 400;
        }

        String method = parts[0];
        String target = parts[1];

        if (!method.equals("GET")) {
            HttpResponses.sendPlainText(out, 405, "Method Not Allowed", "Only GET is supported by this server.");
            return 405;
        }

        String path;
        String query;
        int qIndex = target.indexOf('?');
        if (qIndex >= 0) {
            path = target.substring(0, qIndex);
            query = target.substring(qIndex + 1);
        } else {
            path = target;
            query = "";
        }

        if (path.startsWith("/api/")) {
            return handleService(path, query, out);
        }
        return handleStaticResource(path, out);
    }

    private static int handleService(String path, String query, OutputStream out) throws IOException {
        Map<String, String> params = QueryParams.parse(query);
        try {
            String json;
            switch (path) {
                case "/api/greet" -> json = Services.greeting(params);
                case "/api/square" -> json = Services.square(params);
                case "/api/time" -> json = Services.serverTime(params);
                case "/api/health" -> json = Services.health();
                default -> {
                    HttpResponses.sendJson(out, 404, "Not Found", "{\"error\":\"Unknown service: " + Json.escape(path) + "\"}");
                    return 404;
                }
            }
            HttpResponses.sendJson(out, 200, "OK", json);
            return 200;
        } catch (BadRequestException e) {
            HttpResponses.sendJson(out, 400, "Bad Request", "{\"error\":" + Json.quoted(e.getMessage()) + "}");
            return 400;
        }
    }

    private static int handleStaticResource(String rawPath, OutputStream out) throws IOException {
        String sanitized;
        try {
            sanitized = PathSanitizer.sanitize(rawPath);
        } catch (PathSanitizer.UnsafePathException e) {
            HttpResponses.sendPlainText(out, 400, "Bad Request", "Invalid path.");
            return 400;
        }

        String resourcePath = sanitized.isEmpty() ? INDEX_RESOURCE : sanitized;
        String classpathResource = PUBLIC_ROOT + "/" + resourcePath;

        String contentType = ContentTypes.forPath(resourcePath);
        if (contentType == null) {
            HttpResponses.sendPlainText(out, 404, "Not Found", "Resource not found: " + resourcePath);
            return 404;
        }

        byte[] body = readClasspathResource(classpathResource);
        if (body == null) {
            HttpResponses.sendPlainText(out, 404, "Not Found", "Resource not found: " + resourcePath);
            return 404;
        }

        HttpResponses.send(out, 200, "OK", contentType, body);
        return 200;
    }

    private static byte[] readClasspathResource(String classpathResource) throws IOException {
        try (InputStream resourceStream = HttpServerApp.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (resourceStream == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            resourceStream.transferTo(buffer);
            return buffer.toByteArray();
        }
    }

    /**
     * Reads one CRLF- or LF-terminated line as raw bytes decoded with
     * ISO-8859-1 (safe for HTTP request-line/header bytes). Returns null on
     * end-of-stream with no bytes read.
     */
    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        boolean readAny = false;
        while ((b = in.read()) != -1) {
            readAny = true;
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                buffer.write(b);
            }
        }
        if (!readAny) {
            return null;
        }
        return buffer.toString(StandardCharsets.ISO_8859_1);
    }

    private static void log(String message) {
        System.out.println("[" + Instant.now() + "] " + message);
    }
}
