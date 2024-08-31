package api.jcloudify.app.aws.cloudwatch;

import api.jcloudify.app.model.exception.InternalServerErrorException;
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
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class CloudwatchComponent {
    private final CloudWatchLogsClient cloudWatchLogsClient;

    public List<LogGroup> getLambdaFunctionLogGroupsByName(String functionName) {
        DescribeLogGroupsRequest request = DescribeLogGroupsRequest.builder()
                .logGroupNamePattern(functionName)
                .build();
        try {
            DescribeLogGroupsResponse response = cloudWatchLogsClient.describeLogGroups(request);
            return response.logGroups();
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Error occurred when retrieving log groups of function name={}", functionName);
            throw new InternalServerErrorException(e);
        }
    }

    public List<LogStream> getLogStreams(String logGroupName) {
        DescribeLogStreamsRequest request = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .build();
        try {
            DescribeLogStreamsResponse response = cloudWatchLogsClient.describeLogStreams(request);
            return response.logStreams();
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Error occurred when retrieving log streams of log group name={}", logGroupName);
            throw new InternalServerErrorException(e);
        }
    }

    public List<OutputLogEvent> getLogStreamEvents(String logGroupName, String logStreamName) {
        GetLogEventsRequest request = GetLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .startFromHead(true)
                .build();
        try {
            GetLogEventsResponse response = cloudWatchLogsClient.getLogEvents(request);
            return response.events();
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Error occurred when retrieving log events of log group name={} of log stream name={}", logGroupName, logStreamName);
            throw new InternalServerErrorException(e);
        }
    }
}
