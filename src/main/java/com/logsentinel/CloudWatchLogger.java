package com.logsentinel;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.time.Instant;
import java.util.Collections;

public class CloudWatchLogger {

    private final CloudWatchLogsClient logsClient;
    private final String logGroupName;
    private final String logStreamName;
    private String sequenceToken;

    public CloudWatchLogger(String logGroupName, String logStreamName) {
        this.logsClient = CloudWatchLogsClient.create();
        this.logGroupName = logGroupName;
        this.logStreamName = logStreamName;

        ensureLogGroupAndStream();
        fetchSequenceToken();
    }

    private void ensureLogGroupAndStream() {
        try {
            logsClient.createLogGroup(CreateLogGroupRequest.builder()
                    .logGroupName(logGroupName).build());
        } catch (ResourceAlreadyExistsException ignored) {}

        try {
            logsClient.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName).build());
        } catch (ResourceAlreadyExistsException ignored) {}
    }

    private void fetchSequenceToken() {
        DescribeLogStreamsResponse response = logsClient.describeLogStreams(
                DescribeLogStreamsRequest.builder()
                        .logGroupName(logGroupName)
                        .logStreamNamePrefix(logStreamName)
                        .build()
        );

        if (!response.logStreams().isEmpty()) {
            sequenceToken = response.logStreams().get(0).uploadSequenceToken();
        }
    }

    public void log(String message) {
        InputLogEvent event = InputLogEvent.builder()
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        PutLogEventsRequest.Builder requestBuilder = PutLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .logEvents(Collections.singletonList(event));

        if (sequenceToken != null) {
            requestBuilder.sequenceToken(sequenceToken);
        }

        PutLogEventsResponse response = logsClient.putLogEvents(requestBuilder.build());
        sequenceToken = response.nextSequenceToken();
    }
}
