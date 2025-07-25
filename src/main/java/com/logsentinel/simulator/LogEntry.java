package com.logsentinel.simulator;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single log entry with unique ID, timestamp, log level, service name, and message.
 */


public class LogEntry {
    public String id;          // Unique identifier for the log
    public String timestamp;   // ISO-8601 formatted timestamp of log creation
    public String level;       // Log level: INFO, WARN, ERROR
    public String service;     // Source service name (e.g., login-service)
    public String message;     // Log message describing the event

    /**
     * Constructs a new LogEntry object with given level, service, and message.
     * Automatically assigns a UUID and the current timestamp.
     */
    public LogEntry(String level, String service, String message) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now().toString();
        this.level = level;
        this.service = service;
        this.message = message;
    }


}
