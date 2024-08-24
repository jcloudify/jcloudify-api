package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum.PENDING;
import static api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum.READY;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.jcloudify.app.endpoint.rest.model.StackType.EVENT;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE_SQLITE;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.AppEnvComputeDeployRequested;
import api.jcloudify.app.endpoint.event.model.AppEnvDeployRequested;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.repository.model.EnvDeploymentConf;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.EnvDeploymentConfService;
import api.jcloudify.app.service.StackService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AppEnvDeployRequestedService implements Consumer<AppEnvDeployRequested> {
  private final EventProducer<AppEnvComputeDeployRequested>
      appEnvComputeDeployRequestedEventProducer;
  private final EventProducer<AppEnvDeployRequested> appEnvDeployRequestedEventProducer;
  private final EnvDeploymentConfService envDeploymentConfService;
  private final StackService stackService;
  private final ApplicationService appService;

  @Override
  public void accept(AppEnvDeployRequested appEnvDeployRequested) {
    String userId = appEnvDeployRequested.getUserId();
    String appId = appEnvDeployRequested.getAppId();
    String envId = appEnvDeployRequested.getEnvId();
    switch (appEnvDeployRequested.getIndependentStacksStates()) {
      case NOT_READY -> {
        List<InitiateDeployment> toDeploy = retrieveStacksToDeploy(envId);
        log.info("Cloudformation stacks to deploy: {}", toDeploy);
        stackService.process(toDeploy, userId, appId, envId);
        appEnvDeployRequestedEventProducer.accept(
            List.of(appEnvDeployRequested.toBuilder().independentStacksStates(PENDING).build()));
      }
      case PENDING -> {
        boolean readyToDeployCompute = checkStacksDeploymentState(userId, appId, envId);
        if (readyToDeployCompute) {
          log.info("Compute stack ready to be deployed");
          appEnvDeployRequestedEventProducer.accept(
              List.of(appEnvDeployRequested.toBuilder().independentStacksStates(READY).build()));
        } else {
          log.info("Waiting for independent stacks to be deployed");
          appEnvDeployRequestedEventProducer.accept(
              List.of(appEnvDeployRequested.toBuilder().independentStacksStates(PENDING).build()));
        }
      }
      case READY -> {
        var app = appService.getById(appId);
        BuiltEnvInfo builtEnvInfo = appEnvDeployRequested.getBuiltEnvInfo();
        log.info("Trigger compute stack deployment");
        appEnvComputeDeployRequestedEventProducer.accept(
            List.of(
                AppEnvComputeDeployRequested.builder()
                    .userId(userId)
                    .appId(appId)
                    .envId(envId)
                    .appName(app.getName())
                    .formattedBucketKey(appEnvDeployRequested.getBuiltZipFormattedFilekey())
                    .requestInstant(Instant.now())
                    .environmentType(builtEnvInfo.getEnvironmentType())
                    .build()));
      }
    }
  }

  private List<InitiateDeployment> retrieveStacksToDeploy(String envId) {
    EnvDeploymentConf envDeploymentConf = envDeploymentConfService.getLatestByEnvId(envId);
    List<InitiateDeployment> stacksToDeploy = new ArrayList<>();
    stacksToDeploy.add(new InitiateDeployment().stackType(COMPUTE_PERMISSION));
    if (envDeploymentConf.getEventStackFileKey() != null) {
      stacksToDeploy.add(new InitiateDeployment().stackType(EVENT));
    }
    if (envDeploymentConf.getStorageBucketStackFileKey() != null) {
      stacksToDeploy.add(new InitiateDeployment().stackType(STORAGE_BUCKET));
    }
    if (envDeploymentConf.getStorageDatabaseSqliteStackFileKey() != null) {
      stacksToDeploy.add(new InitiateDeployment().stackType(STORAGE_DATABASE_SQLITE));
    }
    return stacksToDeploy;
  }

  private boolean checkStacksDeploymentState(String userId, String appId, String envId) {
    List<Stack> environmentStacks =
        stackService
            .findAllBy(userId, appId, envId, new PageFromOne(1), new BoundedPageSize(5))
            .data()
            .stream()
            .filter(stack -> !Objects.equals(stack.getStackType(), COMPUTE))
            .toList();
    List<Boolean> areStacksReady =
        environmentStacks.stream()
            .map(stack -> this.isLatestStackEventComplete(userId, appId, envId, stack))
            .toList();
    return areStacksReady.stream().allMatch(state -> state == Boolean.TRUE);
  }

  private boolean isLatestStackEventComplete(
      String userId, String appId, String envId, Stack stack) {
    List<StackEvent> stackEvents =
        stackService
            .getStackEvents(
                userId, appId, envId, stack.getId(), new PageFromOne(1), new BoundedPageSize(5))
            .data()
            .stream()
            .toList();
    if (stackEvents.isEmpty()) {
      return false;
    }
    StackEvent latestEvent = stackEvents.getFirst();
    return Objects.equals(latestEvent.getLogicalResourceId(), stack.getName())
            && Objects.requireNonNull(latestEvent.getResourceStatus())
                .toString()
                .contains("UPDATE_COMPLETE")
        || Objects.requireNonNull(latestEvent.getResourceStatus())
            .toString()
            .contains("CREATE_COMPLETE");
  }
}
