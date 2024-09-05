package api.jcloudify.app.service;

import static api.jcloudify.app.service.StackService.fromStackDataFileToList;
import static api.jcloudify.app.service.StackService.paginate;
import static java.io.File.createTempFile;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.CrupdateLogStreamEventTriggered;
import api.jcloudify.app.endpoint.event.model.CrupdateLogStreamTriggered;
import api.jcloudify.app.endpoint.event.model.PojaEvent;
import api.jcloudify.app.endpoint.rest.mapper.LambdaFunctionLogMapper;
import api.jcloudify.app.endpoint.rest.model.LogGroup;
import api.jcloudify.app.endpoint.rest.model.LogStream;
import api.jcloudify.app.endpoint.rest.model.LogStreamEvent;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LambdaFunctionLogService {
  private final CloudwatchComponent cloudwatchComponent;
  private final ExtendedBucketComponent bucketComponent;
  private final LambdaFunctionLogMapper mapper;
  private final ObjectMapper om;
  private final EventProducer<PojaEvent> eventProducer;

  public void crupdateLogGroups(String functionName, String bucketKey) {
    List<LogGroup> logGroups =
        mapper.toRestLogGroup(cloudwatchComponent.getLambdaFunctionLogGroupsByName(functionName));
    try {
      File logGroupsFile;
      if (bucketComponent.doesExist(bucketKey)) {
        logGroupsFile = bucketComponent.download(bucketKey);
        List<LogGroup> actual = om.readValue(logGroupsFile, new TypeReference<>() {});
        logGroups = mergeAndSortLogGroupList(actual, logGroups);
      } else {
        logGroupsFile = createTempFile("log-groups", ".json");
      }
      om.writeValue(logGroupsFile, logGroups);
      bucketComponent.upload(logGroupsFile, bucketKey);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  public void crupdateLogStreams(String logGroupName, String bucketKey) {
    List<LogStream> logStreams =
        mapper.toRestLogStreams(cloudwatchComponent.getLogStreams(logGroupName));
    try {
      File logStreamsFile;
      if (bucketComponent.doesExist(bucketKey)) {
        logStreamsFile = bucketComponent.download(bucketKey);
        List<LogStream> actual = om.readValue(logStreamsFile, new TypeReference<>() {});
        logStreams = mergeAndSortLogStreamListToSet(actual, logStreams);
      } else {
        logStreamsFile = createTempFile("log-streams", ".json");
      }
      om.writeValue(logStreamsFile, logStreams);
      bucketComponent.upload(logStreamsFile, bucketKey);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void crupdateLogStreamEvents(String logGroupName, String logStreamName, String bucketKey) {
    List<LogStreamEvent> logStreamEvents =
        mapper.toRestLogStreamEvents(
            cloudwatchComponent.getLogStreamEvents(logGroupName, logStreamName));
    try {
      File logStreamEventsFile;
      if (bucketComponent.doesExist(bucketKey)) {
        logStreamEventsFile = bucketComponent.download(bucketKey);
        List<LogStreamEvent> actual = om.readValue(logStreamEventsFile, new TypeReference<>() {});
        logStreamEvents = mergeAndSortLogStreamEventListToSet(actual, logStreamEvents);
      } else {
        logStreamEventsFile = createTempFile("log-stream-events", ".json");
      }
      om.writeValue(logStreamEventsFile, logStreamEvents);
      bucketComponent.upload(logStreamEventsFile, bucketKey);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<LogGroup> mergeAndSortLogGroupList(
      List<LogGroup> actual, List<LogGroup> newLogGroups) {
    Set<LogGroup> mergedList = mergeListToSet(actual, newLogGroups);
    return mergedList.stream()
        .sorted(
            (e1, e2) -> {
              Instant i1 = e1.getCreationDatetime();
              Instant i2 = e2.getCreationDatetime();
              if (i1 == null && i2 == null) return 0;
              if (i1 == null) return 1;
              if (i2 == null) return -1;
              return i2.compareTo(i1);
            })
        .toList();
  }

  private static <T> Set<T> mergeListToSet(List<T> list1, List<T> list2) {
    Set<T> mergedSet = new HashSet<>(list1);
    mergedSet.addAll(list2);
    return mergedSet;
  }

  private static List<LogStream> mergeAndSortLogStreamListToSet(
      List<LogStream> actual, List<LogStream> newLogGStreams) {
    Set<LogStream> mergedList = mergeListToSet(actual, newLogGStreams);
    return mergedList.stream()
        .sorted(
            (e1, e2) -> {
              Instant i1 = e1.getCreationDatetime();
              Instant i2 = e2.getCreationDatetime();
              if (i1 == null && i2 == null) return 0;
              if (i1 == null) return 1;
              if (i2 == null) return -1;
              return i2.compareTo(i1);
            })
        .toList();
  }

  private static List<LogStreamEvent> mergeAndSortLogStreamEventListToSet(
      List<LogStreamEvent> actual, List<LogStreamEvent> newLogGStreams) {
    Set<LogStreamEvent> mergedList = mergeListToSet(actual, newLogGStreams);
    return mergedList.stream()
        .sorted(
            (e1, e2) -> {
              Instant i1 = e1.getTimestamp();
              Instant i2 = e2.getTimestamp();
              if (i1 == null && i2 == null) return 0;
              if (i1 == null) return 1;
              if (i2 == null) return -1;
              return i2.compareTo(i1);
            })
        .toList();
  }

  public Page<LogGroup> getLogGroups(
      String userId,
      String applicationId,
      String environmentId,
      String functionName,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var logGroups = getLogGroups(userId, applicationId, environmentId, functionName);
    return paginate(page, pageSize, logGroups);
  }

  public List<LogGroup> getLogGroups(
      String userId, String applicationId, String environmentId, String functionName) {
    String logGroupBucketKey =
        getLogGroupsBucketKey(userId, applicationId, environmentId, functionName);
    try {
      return fromStackDataFileToList(bucketComponent, om, logGroupBucketKey, LogGroup.class);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  public Page<LogStream> getLogStreams(
      String userId,
      String applicationId,
      String environmentId,
      String functionName,
      String logGroupName,
      PageFromOne page,
      BoundedPageSize pageSize) {
    String logStreamsBucketKey =
        getLogStreamsBucketKey(userId, applicationId, environmentId, functionName, logGroupName);
    eventProducer.accept(
        List.of(
            CrupdateLogStreamTriggered.builder()
                .bucketKey(logStreamsBucketKey)
                .logGroupName(logGroupName)
                .build()));
    try {
      List<LogStream> logStreams =
          fromStackDataFileToList(bucketComponent, om, logStreamsBucketKey, LogStream.class);
      return paginate(page, pageSize, logStreams);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  public Page<LogStreamEvent> getLogStreamEvents(
      String userId,
      String applicationId,
      String environmentId,
      String functionName,
      String logGroupName,
      String logStreamName,
      PageFromOne page,
      BoundedPageSize pageSize) {
    String logStreamEventsBucketKey =
        getLogStreamEventsBucketKey(
            userId, applicationId, environmentId, functionName, logGroupName, logStreamName);
    eventProducer.accept(
        List.of(
            CrupdateLogStreamEventTriggered.builder()
                .bucketKey(logStreamEventsBucketKey)
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .build()));
    try {
      List<LogStreamEvent> logStreams =
          fromStackDataFileToList(
              bucketComponent, om, logStreamEventsBucketKey, LogStreamEvent.class);
      return paginate(page, pageSize, logStreams);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  public static String getLogGroupsBucketKey(
      String userId, String applicationId, String environmentId, String functionName) {
    return String.format(
        "users/%s/apps/%s/envs/%s/function/%s/logGroups/%s",
        userId, applicationId, environmentId, functionName, "log-group.json");
  }

  public static String getLogStreamsBucketKey(
      String userId,
      String applicationId,
      String environmentId,
      String functionName,
      String logGroupName) {
    return String.format(
        "users/%s/apps/%s/envs/%s/function/%s/logGroups/%s/logStreams/%s",
        userId, applicationId, environmentId, functionName, logGroupName, "log-stream.json");
  }

  public static String getLogStreamEventsBucketKey(
      String userId,
      String applicationId,
      String environmentId,
      String functionName,
      String logGroupName,
      String logStreamName) {
    return String.format(
        "users/%s/apps/%s/envs/%s/function/%s/logGroups/%s/logStreams/%s/logEvents/%s",
        userId,
        applicationId,
        environmentId,
        functionName,
        logGroupName,
        logStreamName,
        "log-events.json");
  }
}
