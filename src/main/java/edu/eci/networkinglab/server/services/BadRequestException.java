package edu.eci.networkinglab.server.services;

/**
 * Thrown by a service when the request is well-formed HTTP but the input
 * for that service is missing or invalid. Mapped to HTTP 400 by the server.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
