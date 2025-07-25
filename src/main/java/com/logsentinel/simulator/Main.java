package com.logsentinel.simulator;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;

/**
 * Entry point to the log simulator application.
 * Connects to AWS Kinesis and continuously sends simulated log entries.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String streamName = "LogStream"; // Replace this with your Kinesis stream name
        // Create a Kinesis client configured to use the US East (N. Virginia) region
        KinesisClient client = KinesisClient.builder()
                .region(Region.US_EAST_1)
                .build();

        // Instantiate the log producer with the Kinesis client and stream name
        LogProducer producer = new LogProducer(client, streamName);

        // Loop indefinitely, sending one log every 2 seconds
        while (true) {
            producer.generateAndSendLog();
            Thread.sleep(2000); // Pause for 2 seconds between logs
        }
    }
}
