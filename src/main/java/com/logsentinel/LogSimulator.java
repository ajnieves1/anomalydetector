package com.logsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.awssdk.core.SdkBytes;


import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Random;

/**
 * LogSimulator generates random log events and sends them to an AWS Kinesis stream.
 */
public class LogSimulator {

    // Your Kinesis stream name
    private static final String STREAM_NAME = "SusLogStream"; // If changing name of stream, change this field

    // Used to convert objects to JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    // Sample log levels
    private static final String[] LEVELS = {"INFO", "WARN", "ERROR", "DEBUG", "TRACE"};

    public static void main(String[] args) throws Exception {
        Region region = Region.US_EAST_1; 

        // Initialize AWS Kinesis client
        KinesisClient kinesisClient = KinesisClient.builder()
                .region(region)
                .build();

        // Loop to simulate sending logs
        while (true) {
            LogEvent log = generateRandomLog();
            String jsonLog = mapper.writeValueAsString(log);

            // Send to Kinesis
            PutRecordRequest request = PutRecordRequest.builder()
                    .streamName(STREAM_NAME)
                    .partitionKey(log.userId)  // ensures order per user
                    .data(SdkBytes.fromByteArray(jsonLog.getBytes()))
                    .build();

            PutRecordResponse response = kinesisClient.putRecord(request);
            System.out.println("Sent log to shard: " + response.shardId() + " -> " + jsonLog);

            Thread.sleep(1000); // Send one log per second
        }
    }

    /**
     * Generates a random log event with fake data.
     */
    private static LogEvent generateRandomLog() {
        Random rand = new Random();
        return new LogEvent(
                Instant.now().toString(),
                "user" + rand.nextInt(100),
                LEVELS[rand.nextInt(LEVELS.length)],
                "Suspicious activity detected in system module " + rand.nextInt(5)
        );
    }

    /**
     * Represents the structure of a log event.
     */
    public static class LogEvent {
        public String timestamp;
        public String userId;
        public String level;
        public String message;

        public LogEvent(String timestamp, String userId, String level, String message) {
            this.timestamp = timestamp;
            this.userId = userId;
            this.level = level;
            this.message = message;
        }
    }
}
