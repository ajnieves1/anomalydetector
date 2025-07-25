package com.logsentinel;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;

/**
 * Entry point to the log simulator application.
 * Connects to AWS Kinesis and continuously sends simulated log entries.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String streamName = "SusLogStream"; // Replace with your actual stream
        String logGroupName = "LogGroup"; // Replace with your desired CloudWatch log group
        String logStreamName = "SusLogStream";      // Replace with your desired log stream
        String region = "us-east-1";              // Replace with your region

        // Step 1: Create the Kinesis client
        KinesisClient kinesisClient = KinesisClient.builder()
                .region(Region.of(region))
                .build();

        // Step 2: Initialize CloudWatchLogger
        CloudWatchLogger cloudWatchLogger = new CloudWatchLogger(logGroupName, logStreamName);

        // Step 3: Create the LogProducer and send logs
        LogProducer producer = new LogProducer(kinesisClient, streamName, cloudWatchLogger);
        producer.generateAndSendLog();
    }
}



