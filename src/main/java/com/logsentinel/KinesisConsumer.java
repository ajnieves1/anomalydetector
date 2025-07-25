package com.logsentinel;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;
import software.amazon.kinesis.retrieval.KinesisClientRecord;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;

// This is a test

import java.nio.charset.StandardCharsets;
import java.util.List;

public class KinesisConsumer {

    private static final String STREAM_NAME = "SusLogStream";
    private static final String APPLICATION_NAME = "LogConsumerApp"; // KCL tracks checkpoints under this name

    public static void main(String[] args) {
        KinesisAsyncClient kinesisClient = KinesisAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ConfigsBuilder configsBuilder = new ConfigsBuilder(
                STREAM_NAME,
                APPLICATION_NAME,
                kinesisClient,
                dynamoDbClient,
                cloudWatchClient,
                "worker-1",
                new LogRecordProcessorFactory()
        );

        Scheduler scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig()
        );

        System.out.println("Starting Kinesis Consumer...");
        scheduler.run();
    }

    static class LogRecordProcessorFactory implements ShardRecordProcessorFactory {
        @Override
        public ShardRecordProcessor shardRecordProcessor() {
            return new LogRecordProcessor();
        }
    }

    static class LogRecordProcessor implements ShardRecordProcessor {

        @Override
        public void initialize(InitializationInput initializationInput) {
            System.out.println("Initializing record processor for shard: " + initializationInput.shardId());
        }

        @Override
        public void processRecords(ProcessRecordsInput processRecordsInput) {
            List<KinesisClientRecord> records = processRecordsInput.records();
            System.out.println(">>> Received " + records.size() + " records");
            for (KinesisClientRecord record : records) {
                String data = StandardCharsets.UTF_8.decode(record.data()).toString();
                System.out.println(">>> Consumed: " + data);
            }
            // checkpoint after processing records
            try {
                processRecordsInput.checkpointer().checkpoint();
            } catch (ShutdownException | InvalidStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void leaseLost(LeaseLostInput leaseLostInput) {
            System.out.println("Lease lost");
        }

        @Override
        public void shardEnded(ShardEndedInput shardEndedInput) {
            System.out.println("Shard ended");
            try {
                shardEndedInput.checkpointer().checkpoint();
            } catch (ShutdownException | InvalidStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
            System.out.println("Shutdown requested");
            try {
                shutdownRequestedInput.checkpointer().checkpoint();
            } catch (ShutdownException | InvalidStateException e) {
                e.printStackTrace();
            }
        }
    }
}
