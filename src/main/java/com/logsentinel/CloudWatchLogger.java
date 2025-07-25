package com.logsentinel;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

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
    }

    private void ensureLogGroupAndStream() {
        try {
            logsClient.createLogGroup(CreateLogGroupRequest.builder().logGroupName(logGroupName).build());
        } catch (ResourceAlreadyExistsException ignored) {}

        try {
            logsClient.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .build());
        } catch (ResourceAlreadyExistsException ignored) {}
    }

    public void log(String message) {
        InputLogEvent logEvent = InputLogEvent.builder()
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        PutLogEventsRequest.Builder requestBuilder = PutLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .logEvents(Collections.singletonList(logEvent));

        if (sequenceToken != null) {
            requestBuilder.sequenceToken(sequenceToken);
        }

        PutLogEventsResponse response = logsClient.putLogEvents(requestBuilder.build());
        sequenceToken = response.nextSequenceToken();
    }
}
