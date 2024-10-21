package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum.PENDING;
import static api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum.READY;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.*;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.CREATE_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.CREATE_FAILED;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.ROLLBACK_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.ROLLBACK_FAILED;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UPDATE_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UPDATE_FAILED;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UPDATE_ROLLBACK_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UPDATE_ROLLBACK_FAILED;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.jcloudify.app.endpoint.rest.model.StackType.EVENT;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE_SQLITE;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.AppEnvComputeDeployRequested;
import api.jcloudify.app.endpoint.event.model.AppEnvDeployRequested;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdateTriggered;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackDeployment;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.endpoint.rest.model.StackResourceStatusType;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.repository.model.EnvDeploymentConf;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.EnvDeploymentConfService;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.StackService;
import api.jcloudify.app.service.workflows.DeploymentStateService;
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
  private final EnvironmentService environmentService;
  private final DeploymentStateService deploymentStateService;
  private final EventProducer<ComputeStackCrupdateTriggered>
      computeStackCrupdateTriggeredEventProducer;

  @Override
  public void accept(AppEnvDeployRequested appEnvDeployRequested) {
    String userId = appEnvDeployRequested.getUserId();
    String appId = appEnvDeployRequested.getAppId();
    String envId = appEnvDeployRequested.getEnvId();
    String appEnvDeploymentId = appEnvDeployRequested.getAppEnvDeploymentId();
    switch (appEnvDeployRequested.getCurrentIndependentStacksState()) {
      case NOT_READY -> {
        List<StackDeployment> toDeploy = retrieveStacksToDeploy(envId);
        log.info("Cloudformation stacks to deploy: {}", toDeploy);
        stackService.processDeployment(toDeploy, userId, appId, envId);
        appEnvDeployRequestedEventProducer.accept(
            List.of(
                appEnvDeployRequested.toBuilder().currentIndependentStacksState(PENDING).build()));
        deploymentStateService.save(appEnvDeploymentId, INDEPENDENT_STACKS_DEPLOYMENT_IN_PROGRESS);
      }
      case PENDING -> {
        IndependentStacksDeploymentStateEnum independentStacksDeploymentState =
            checkStacksDeploymentState(userId, appId, envId);
        switch (independentStacksDeploymentState) {
          case PENDING -> {
            log.info("Waiting for independent stacks to be deployed");
            appEnvDeployRequestedEventProducer.accept(
                List.of(
                    appEnvDeployRequested.toBuilder()
                        .currentIndependentStacksState(PENDING)
                        .build()));
          }
          case DEPLOYED -> {
            log.info("Compute stack ready to be deployed");
            deploymentStateService.save(appEnvDeploymentId, INDEPENDENT_STACKS_DEPLOYED);
            appEnvDeployRequestedEventProducer.accept(
                List.of(
                    appEnvDeployRequested.toBuilder()
                        .currentIndependentStacksState(READY)
                        .build()));
          }
          case NOT_DEPLOYED -> {
            log.info("Independent stacks deployment failed");
            deploymentStateService.save(appEnvDeploymentId, INDEPENDENT_STACKS_DEPLOYMENT_FAILED);
          }
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
                    .appEnvDeploymentId(appEnvDeploymentId)
                    .build()));
        deploymentStateService.save(appEnvDeploymentId, COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS);
        var env = environmentService.getById(envId);
        String stackName =
            String.format("%s-compute-%s", env.getFormattedEnvironmentType(), app.getName());
        computeStackCrupdateTriggeredEventProducer.accept(
            List.of(
                ComputeStackCrupdateTriggered.builder()
                    .userId(userId)
                    .appId(appId)
                    .envId(envId)
                    .stackName(stackName)
                    .appEnvDeploymentId(appEnvDeploymentId)
                    .build()));
      }
    }
  }

  private List<StackDeployment> retrieveStacksToDeploy(String envId) {
    EnvDeploymentConf envDeploymentConf = envDeploymentConfService.getLatestByEnvId(envId);
    List<StackDeployment> stacksToDeploy = new ArrayList<>();
    stacksToDeploy.add(new StackDeployment().stackType(COMPUTE_PERMISSION));
    if (envDeploymentConf.getEventStackFileKey() != null) {
      stacksToDeploy.add(new StackDeployment().stackType(EVENT));
    }
    if (envDeploymentConf.getStorageBucketStackFileKey() != null) {
      stacksToDeploy.add(new StackDeployment().stackType(STORAGE_BUCKET));
    }
    if (envDeploymentConf.getStorageDatabaseSqliteStackFileKey() != null) {
      stacksToDeploy.add(new StackDeployment().stackType(STORAGE_DATABASE_SQLITE));
    }
    return stacksToDeploy;
  }

  private IndependentStacksDeploymentStateEnum checkStacksDeploymentState(
      String userId, String appId, String envId) {
    List<Stack> environmentStacks =
        stackService
            .findAllBy(userId, appId, envId, new PageFromOne(1), new BoundedPageSize(5))
            .data()
            .stream()
            .filter(stack -> !Objects.equals(stack.getStackType(), COMPUTE))
            .toList();
    List<IndependentStacksDeploymentStateEnum> stackDeploymentStates =
        environmentStacks.stream()
            .map(stack -> this.checkStackDeploymentState(userId, appId, envId, stack))
            .toList();
    if (stackDeploymentStates.stream()
        .allMatch(IndependentStacksDeploymentStateEnum.DEPLOYED::equals))
      return IndependentStacksDeploymentStateEnum.DEPLOYED;
    if (stackDeploymentStates.stream()
        .noneMatch(IndependentStacksDeploymentStateEnum.NOT_DEPLOYED::equals))
      return IndependentStacksDeploymentStateEnum.PENDING;
    return IndependentStacksDeploymentStateEnum.NOT_DEPLOYED;
  }

  private IndependentStacksDeploymentStateEnum checkStackDeploymentState(
      String userId, String appId, String envId, Stack stack) {
    List<StackEvent> stackEvents =
        stackService
            .getStackEvents(
                userId, appId, envId, stack.getId(), new PageFromOne(1), new BoundedPageSize(5))
            .data()
            .stream()
            .toList();
    if (!stackEvents.isEmpty()) {
      StackEvent latestEvent = stackEvents.getFirst();
      if (Objects.equals(stack.getName(), latestEvent.getLogicalResourceId())) {
        StackResourceStatusType latestResourceStatus = latestEvent.getResourceStatus();
        if (CREATE_COMPLETE.equals(latestResourceStatus)
            || UPDATE_COMPLETE.equals(latestResourceStatus)) {
          return IndependentStacksDeploymentStateEnum.DEPLOYED;
        }
        if (CREATE_FAILED.equals(latestResourceStatus)
            || UPDATE_FAILED.equals(latestResourceStatus)
            || ROLLBACK_COMPLETE.equals(latestResourceStatus)
            || ROLLBACK_FAILED.equals(latestResourceStatus)
            || UPDATE_ROLLBACK_COMPLETE.equals(latestResourceStatus)
            || UPDATE_ROLLBACK_FAILED.equals(latestResourceStatus)) {
          return IndependentStacksDeploymentStateEnum.NOT_DEPLOYED;
        }
      }
    }
    return IndependentStacksDeploymentStateEnum.PENDING;
  }

  private enum IndependentStacksDeploymentStateEnum {
    PENDING,
    DEPLOYED,
    NOT_DEPLOYED
  }
}
