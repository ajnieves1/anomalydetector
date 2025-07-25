package com.logsentinel.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.core.SdkBytes;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Responsible for generating simulated log entries and sending them to an AWS Kinesis stream.
 */
public class LogProducer {
    private static final String[] SERVICES = {"login-service", "user-service", "payment-service"};
    private static final String[] LEVELS = {"INFO", "WARN", "ERROR"};
    private static final ObjectMapper mapper = new ObjectMapper(); // For JSON serialization
    private static final Random random = new Random();

    private final KinesisClient client;   // AWS Kinesis client used to send records
    private final String streamName;      // Target Kinesis stream name

    /**
     * Constructor to initialize the LogProducer with Kinesis client and stream name.
     * @param client AWS Kinesis client to use for sending data
     * @param streamName Name of the Kinesis stream to send logs to
     */
    public LogProducer(KinesisClient client, String streamName) {
        this.client = client;
        this.streamName = streamName;
    }

    /**
     * Generates a random log entry and sends it as a JSON record to the configured Kinesis stream.
     * @throws Exception if sending the record fails
     */
    public void generateAndSendLog() throws Exception {
        // Randomly select a log level and service name
        String level = LEVELS[random.nextInt(LEVELS.length)];
        String service = SERVICES[random.nextInt(SERVICES.length)];

        // Create a message based on log level and service
        String message = switch (level) {
            case "ERROR" -> "Unhandled exception in " + service;
            case "WARN" -> "Slow response detected in " + service;
            default -> "Request completed successfully in " + service;
        };

        // Create a new LogEntry object
        LogEntry log = new LogEntry(level, service, message);

        // Serialize LogEntry to JSON string
        String json = mapper.writeValueAsString(log);

        // Build Kinesis PutRecord request
        PutRecordRequest request = PutRecordRequest.builder()
                .streamName(streamName)
                .partitionKey(log.service)  // Partition by service name for distribution
                .data(SdkBytes.fromString(json, StandardCharsets.UTF_8))
                .build();

        // Send record to Kinesis
        client.putRecord(request);

        // Print confirmation to console
        System.out.println("Sent log: " + json);
    }
}
