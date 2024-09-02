package api.jcloudify.app.service;

import static api.jcloudify.app.service.StackService.fromStackDataFileToList;
import static api.jcloudify.app.service.StackService.paginate;
import static java.io.File.createTempFile;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.rest.mapper.LambdaFunctionLogMapper;
import api.jcloudify.app.endpoint.rest.model.LogGroup;
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

  public void crupdateLogGroups(String functionName, String bucketKey) {
    List<LogGroup> logGroups =
        mapper.toRest(cloudwatchComponent.getLambdaFunctionLogGroupsByName(functionName));
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
      throw new RuntimeException(e);
    }
  }

  private static List<LogGroup> mergeAndSortLogGroupList(
      List<LogGroup> actual, List<LogGroup> newLogGroups) {
    Set<LogGroup> mergedSet = new HashSet<>(actual);
    mergedSet.addAll(newLogGroups);
    return mergedSet.stream()
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

  public Page<LogGroup> getLogGroups(
      String userId,
      String applicationId,
      String environmentId,
      String functionName,
      PageFromOne page,
      BoundedPageSize pageSize) {
    String logGroupBucketKey =
        getLogGroupsBucketKey(userId, applicationId, environmentId, functionName);
    try {
      List<LogGroup> logGroups =
          fromStackDataFileToList(bucketComponent, om, logGroupBucketKey, LogGroup.class);
      return paginate(page, pageSize, logGroups);
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
}
