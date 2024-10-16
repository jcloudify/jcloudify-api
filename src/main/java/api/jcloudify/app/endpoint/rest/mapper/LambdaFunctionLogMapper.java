package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.LogGroup;
import api.jcloudify.app.endpoint.rest.model.LogStream;
import api.jcloudify.app.endpoint.rest.model.LogStreamEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;

@Component
public class LambdaFunctionLogMapper {
  private LogGroup toRestLogGroup(
      software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup awsLogGroup) {
    return new LogGroup()
        .name(awsLogGroup.logGroupName())
        .creationDatetime(Instant.ofEpochMilli(awsLogGroup.creationTime()));
  }

  public List<LogGroup> toRestLogGroup(
      List<software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup> awsLogGroups) {
    return awsLogGroups.stream().map(this::toRestLogGroup).toList();
  }

  private LogStream toRestLogStream(
      software.amazon.awssdk.services.cloudwatchlogs.model.LogStream awsLogStream) {
    return new LogStream()
        .name(awsLogStream.logStreamName())
        .creationDatetime(Instant.ofEpochMilli(awsLogStream.creationTime()))
        .firstEventDatetime(Instant.ofEpochMilli(awsLogStream.firstEventTimestamp()))
        .lastEventDatetime(Instant.ofEpochMilli(awsLogStream.lastEventTimestamp()));
  }

  public List<LogStream> toRestLogStreams(
      List<software.amazon.awssdk.services.cloudwatchlogs.model.LogStream> awsLogStreams) {
    return awsLogStreams.stream().map(this::toRestLogStream).toList();
  }

  public LogStreamEvent toRestLogStreamEvent(OutputLogEvent awsLogEvent) {
    return new LogStreamEvent()
        .message(awsLogEvent.message())
        .timestamp(Instant.ofEpochMilli(awsLogEvent.timestamp()));
  }

  public List<LogStreamEvent> toRestLogStreamEvents(List<OutputLogEvent> awsLogEvents) {
    return awsLogEvents.stream().map(this::toRestLogStreamEvent).toList();
  }
}
