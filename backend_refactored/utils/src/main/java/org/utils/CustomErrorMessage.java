package org.utils;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

// make it JSON serializable with the jackson annotations
// Passing non JSON-Serializable object to the ResponseEntity class would raise
// a HttpMediaTypeNotAcceptableException
@JsonPropertyOrder({"statusCode", "timestamp", "message", "description"})
public class CustomErrorMessage {

    private int statusCode;

    private LocalDateTime timestamp;

    private String message;

    private String description;

    public CustomErrorMessage(
            int statusCode,
            LocalDateTime timestamp,
            String message,
            String description) {

        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
    }

    // private no-argument constructor for the jackson package
    @SuppressWarnings("unused")
    private CustomErrorMessage() {}

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}
