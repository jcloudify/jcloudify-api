package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.LogGroup;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LambdaFunctionLogMapper {
  public LogGroup toRest(
      software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup AwsLogGroup) {
    return new LogGroup()
        .name(AwsLogGroup.logGroupName())
        .creationDatetime(Instant.ofEpochMilli(AwsLogGroup.creationTime()));
  }

  public List<LogGroup> toRest(
      List<software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup> awsLogGroups) {
    return awsLogGroups.stream().map(this::toRest).toList();
  }
}
