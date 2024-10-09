package api.jcloudify.app.service.event;

import static api.jcloudify.app.service.LambdaFunctionLogService.getLogGroupsBucketKey;
import static api.jcloudify.app.service.LambdaFunctionLogService.getLogStreamsBucketKey;
import static api.jcloudify.app.service.StackService.fromStackDataFileToList;
import static api.jcloudify.app.service.event.ComputeStackCrupdateCompletedService.getFunctionNames;

import api.jcloudify.app.endpoint.event.model.RefreshUsersLogStreamsTriggered;
import api.jcloudify.app.endpoint.rest.model.LogGroup;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.User;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.ComputeStackResource;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.ComputeStackResourceService;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.LambdaFunctionLogService;
import api.jcloudify.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class RefreshUsersLogStreamsTriggeredService
    implements Consumer<RefreshUsersLogStreamsTriggered> {
  private final UserService userService;
  private final ApplicationService applicationService;
  private final EnvironmentService environmentService;
  private final ComputeStackResourceService computeStackResourceService;
  private final ExtendedBucketComponent bucketComponent;
  private final ObjectMapper om;
  private final LambdaFunctionLogService logService;

  @Override
  public void accept(RefreshUsersLogStreamsTriggered refreshUsersLogStreamsTriggered) {
    this.crupdateUsersLogStreams();
  }

  private List<Application> getUserApplications(String userId) {
    return applicationService.findAllByUserId(userId);
  }

  private List<Environment> getApplicationEnvironments(String appId) {
    return environmentService.findAllByApplicationId(appId);
  }

  public static List<String> getEnvironmentFunctions(
      String envId, ComputeStackResourceService computeStackResourceService) {
    List<ComputeStackResource> computeStackResources =
        computeStackResourceService.findAllByEnvironmentId(envId);
    return computeStackResources.stream()
        .flatMap(resource -> getFunctionNames(resource).stream())
        .toList();
  }

  public static List<LogGroup> getLogGroups(
      String bucketKey, ExtendedBucketComponent bucketComponent, ObjectMapper om) {
    try {
      return fromStackDataFileToList(bucketComponent, om, bucketKey, LogGroup.class);
    } catch (IOException e) {
      log.info("Error occurred during log groups fetch in bucket key name={}", bucketKey);
      throw new RuntimeException(e);
    }
  }

  private void crupdateUsersLogStreams() {
    List<User> users = userService.findAll();
    users.forEach(this::crupdateApplicationsLogStreams);
  }

  private void crupdateApplicationsLogStreams(User user) {
    String userId = user.getId();
    log.info("Crupdate applications log streams of user id={}", userId);
    List<Application> applications = getUserApplications(userId);
    applications.forEach(
        application -> {
          crupdateEnvironmentsLogStreams(user, application);
        });
  }

  private void crupdateEnvironmentsLogStreams(User user, Application application) {
    log.info("Processing log streams crupdate for application id={}", application.getId());
    List<Environment> environments = getApplicationEnvironments(application.getId());
    environments.forEach(
        environment -> {
          crupdateFunctionsLogStreams(user, application, environment);
        });
  }

  private void crupdateFunctionsLogStreams(
      User user, Application application, Environment environment) {
    List<String> functionNames =
        getEnvironmentFunctions(environment.getId(), computeStackResourceService);
    functionNames.forEach(
        functionName -> {
          crupdateLogGroupsLogStreams(user, application, environment, functionName);
        });
  }

  private void crupdateLogGroupsLogStreams(
      User user, Application application, Environment environment, String functionName) {
    String logGroupBucketKey =
        getLogGroupsBucketKey(user.getId(), application.getId(), environment.getId(), functionName);
    List<LogGroup> logGroupList = getLogGroups(logGroupBucketKey, bucketComponent, om);
    logGroupList.forEach(
        logGroup -> {
          crupdateLogStreams(user, application, environment, functionName, logGroup);
        });
  }

  private void crupdateLogStreams(
      User user,
      Application application,
      Environment environment,
      String functionName,
      LogGroup logGroup) {
    String logStreamBucketKey =
        getLogStreamsBucketKey(
            user.getId(),
            application.getId(),
            environment.getId(),
            functionName,
            logGroup.getName());
    logService.crupdateLogStreams(logGroup.getName(), logStreamBucketKey);
  }
}
