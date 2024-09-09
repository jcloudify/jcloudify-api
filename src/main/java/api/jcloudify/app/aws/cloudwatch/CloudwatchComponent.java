package api.jcloudify.app.aws.cloudwatch;

import api.jcloudify.app.model.exception.InternalServerErrorException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogGroupsIterable;

@Component
@AllArgsConstructor
@Slf4j
public class CloudwatchComponent {
  private final CloudWatchLogsClient cloudWatchLogsClient;

  public List<LogGroup> getLambdaFunctionLogGroupsByNamePattern(String namePattern) {
    DescribeLogGroupsRequest request =
        DescribeLogGroupsRequest.builder().logGroupNamePattern(namePattern).build();

    try {
      DescribeLogGroupsResponse response = cloudWatchLogsClient.describeLogGroups(request);
      return response.logGroups();
    } catch (AwsServiceException | SdkClientException e) {
      log.error("Error occurred when retrieving log groups of name pattern={}", namePattern);
      throw new InternalServerErrorException(e);
    }
  }

  public Iterator<DescribeLogGroupsResponse> getLambdaFunctionLogGroupsByNamePatternIterator(
      String namePattern) {
    try {
      log.info("fetching logs for {}", namePattern);
      DescribeLogGroupsIterable response =
          cloudWatchLogsClient.describeLogGroupsPaginator(
              req -> req.logGroupNamePattern(namePattern));
      return response.iterator();
    } catch (AwsServiceException | SdkClientException e) {
      log.error("Error occurred when retrieving log groups of name pattern={}", namePattern);
      throw new InternalServerErrorException(e);
    }
  }

  public List<LogStream> getLogStreams(String logGroupName) {
    DescribeLogStreamsRequest request =
        DescribeLogStreamsRequest.builder().logGroupName(logGroupName).build();
    try {
      DescribeLogStreamsResponse response = cloudWatchLogsClient.describeLogStreams(request);
      return response.logStreams();
    } catch (AwsServiceException | SdkClientException e) {
      log.error("Error occurred when retrieving log streams of log group name={}", logGroupName);
      throw new InternalServerErrorException(e);
    }
  }

  public List<OutputLogEvent> getLogStreamEvents(String logGroupName, String logStreamName) {
    GetLogEventsRequest request =
        GetLogEventsRequest.builder()
            .logGroupName(logGroupName)
            .logStreamName(logStreamName)
            .startFromHead(true)
            .build();
    try {
      GetLogEventsResponse response = cloudWatchLogsClient.getLogEvents(request);
      return response.events();
    } catch (AwsServiceException | SdkClientException e) {
      log.error(
          "Error occurred when retrieving log events of log group name={} of log stream name={}",
          logGroupName,
          logStreamName);
      throw new InternalServerErrorException(e);
    }
  }

  public String initiateLogInsightsQuery(
      String queryString, Instant startTime, Instant endTime, List<String> logGroupNames) {
    if (logGroupNames.size() > 50) {
      throw new InternalServerErrorException(
          "cannot start insights query with more than 50 log group names");
    }
    log.info("querying with {} {}", logGroupNames.size(), logGroupNames);
    var query =
        cloudWatchLogsClient.startQuery(
            sqr ->
                sqr.queryString(queryString)
                    .startTime(startTime.getEpochSecond())
                    .endTime(endTime.getEpochSecond())
                    .logGroupNames(logGroupNames));
    return query.queryId();
  }

  public GetQueryResultsResponse getQueryResult(String queryId) {
    return cloudWatchLogsClient.getQueryResults(gqr -> gqr.queryId(queryId));
  }
}
