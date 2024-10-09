package api.jcloudify.app.service.event;

import static api.jcloudify.app.service.LambdaFunctionLogService.getLogGroupsBucketKey;
import static api.jcloudify.app.service.LambdaFunctionLogService.getLogStreamEventsBucketKey;
import static api.jcloudify.app.service.LambdaFunctionLogService.getLogStreamsBucketKey;
import static api.jcloudify.app.service.StackService.fromStackDataFileToList;
import static api.jcloudify.app.service.event.RefreshUsersLogStreamsTriggeredService.getEnvironmentFunctions;
import static api.jcloudify.app.service.event.RefreshUsersLogStreamsTriggeredService.getLogGroups;

import api.jcloudify.app.endpoint.event.model.RefreshUsersLogStreamEventsTriggered;
import api.jcloudify.app.endpoint.rest.model.LogGroup;
import api.jcloudify.app.endpoint.rest.model.LogStream;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.User;
import api.jcloudify.app.repository.model.Application;
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
public class RefreshUsersLogStreamEventsTriggeredService
    implements Consumer<RefreshUsersLogStreamEventsTriggered> {
  private final UserService userService;
  private final ApplicationService applicationService;
  private final EnvironmentService environmentService;
  private final ComputeStackResourceService computeStackResourceService;
  private final ExtendedBucketComponent bucketComponent;
  private final ObjectMapper om;
  private final LambdaFunctionLogService logService;

  public static List<LogStream> getLogStreams(
      String bucketKey, ExtendedBucketComponent bucketComponent, ObjectMapper om) {
    try {
      return fromStackDataFileToList(bucketComponent, om, bucketKey, LogStream.class);
    } catch (IOException e) {
      log.info("Error occurred during log groups fetch in bucket key name={}", bucketKey);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void accept(RefreshUsersLogStreamEventsTriggered refreshUsersLogStreamEventsTriggered) {
    this.crupdateUsersLogStreamEvents();
  }

  private void crupdateUsersLogStreamEvents() {
    List<User> users = userService.findAll();
    users.forEach(this::crupdateUserLogStreamEvents);
  }

  private void crupdateUserLogStreamEvents(User user) {
    String userId = user.getId();
    log.info("Crupdate applications log streams of user id={}", userId);
    List<Application> applications = applicationService.findAllByUserId(userId);
    applications.forEach(
        application -> {
          crupdateApplicationLogStreamEvents(user, application);
        });
  }

  private void crupdateApplicationLogStreamEvents(User user, Application application) {
    String applicationId = application.getId();
    log.info("Processing log streams crupdate for application id={}", applicationId);
    List<Environment> environments = environmentService.findAllByApplicationId(applicationId);
    environments.forEach(
        environment -> {
          crupdateEnvironmentLogStreamEvents(user, application, environment);
        });
  }

  private void crupdateEnvironmentLogStreamEvents(
      User user, Application application, Environment environment) {
    List<String> functionNames =
        getEnvironmentFunctions(environment.getId(), computeStackResourceService);
    functionNames.forEach(
        functionName -> {
          crupdateFunctionLogStreamEvents(user, application, environment, functionName);
        });
  }

  private void crupdateFunctionLogStreamEvents(
      User user, Application application, Environment environment, String functionName) {
    String logGroupBucketKey =
        getLogGroupsBucketKey(user.getId(), application.getId(), environment.getId(), functionName);
    List<LogGroup> logGroupList = getLogGroups(logGroupBucketKey, bucketComponent, om);
    logGroupList.forEach(
        logGroup -> {
          crupdateLogStreamLogStreamEvents(user, application, environment, functionName, logGroup);
        });
  }

  private void crupdateLogStreamLogStreamEvents(
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
    List<LogStream> logStreamList = getLogStreams(logStreamBucketKey, bucketComponent, om);
    logStreamList.forEach(
        logStream -> {
          crupdateUsersLogStreamEvents(
              user, application, environment, functionName, logGroup, logStream);
        });
  }

  private void crupdateUsersLogStreamEvents(
      User user,
      Application application,
      Environment environment,
      String functionName,
      LogGroup logGroup,
      LogStream logStream) {
    String logStreamEventBucketKey =
        getLogStreamEventsBucketKey(
            user.getId(),
            application.getId(),
            environment.getId(),
            functionName,
            logGroup.getName(),
            logStream.getName());
    logService.crupdateLogStreamEvents(
        logGroup.getName(), logStream.getName(), logStreamEventBucketKey);
  }
}
