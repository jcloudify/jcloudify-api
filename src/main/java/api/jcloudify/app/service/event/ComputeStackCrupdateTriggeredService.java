package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.*;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE;
import static api.jcloudify.app.service.StackService.STACK_EVENT_FILENAME;
import static api.jcloudify.app.service.StackService.getStackEventsBucketKey;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdateTriggered;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.dao.StackDao;
import api.jcloudify.app.repository.model.AppEnvironmentDeployment;
import api.jcloudify.app.repository.model.DeploymentState;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.AppEnvironmentDeploymentService;
import api.jcloudify.app.service.StackService;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ComputeStackCrupdateTriggeredService
    implements Consumer<ComputeStackCrupdateTriggered> {
  private final StackService stackService;
  private final AppEnvironmentDeploymentService appEnvironmentDeploymentService;
  private final StackDao stackDao;
  private final EventProducer<StackCrupdated> stackCrupdatedEventProducer;
  private final EventProducer<ComputeStackCrupdateTriggered>
      computeStackCrupdateTriggeredEventProducer;

  @Override
  public void accept(ComputeStackCrupdateTriggered computeStackCrupdateTriggered) {
    String userId = computeStackCrupdateTriggered.getUserId();
    String applicationId = computeStackCrupdateTriggered.getAppId();
    String environmentId = computeStackCrupdateTriggered.getEnvId();
    String stackName = computeStackCrupdateTriggered.getStackName();
    String appEnvDeploymentId = computeStackCrupdateTriggered.getAppEnvDeploymentId();
    Optional<String> cfStackId = stackService.getCloudformationStackId(stackName);
    Optional<DeploymentState> latestState = getDeploymentLatestState(appEnvDeploymentId);
    Optional<Stack> stack = stackDao.findByCriteria(applicationId, environmentId, COMPUTE);
    if (latestState.isEmpty()) {
      log.error("No state has been found for deployment id={}", appEnvDeploymentId);
      return;
    }
    DeploymentStateEnum latestStatus = latestState.get().getProgressionStatus();
    if (COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS.equals(latestStatus)) {
      if (cfStackId.isPresent()) {
        Stack saved;
        if (stack.isPresent()) {
          Stack toUpdate = stack.get();
          toUpdate.toBuilder().cfStackId(cfStackId.get()).name(stackName).build();
          saved = stackService.save(toUpdate);
        } else {
          saved =
              stackService.save(
                  Stack.builder()
                      .name(stackName)
                      .cfStackId(cfStackId.get())
                      .applicationId(applicationId)
                      .environmentId(environmentId)
                      .type(COMPUTE)
                      .build());
        }
        String stackEventsBucketKey =
            getStackEventsBucketKey(
                userId,
                saved.getApplicationId(),
                saved.getEnvironmentId(),
                saved.getId(),
                STACK_EVENT_FILENAME);
        stackService.crupdateStackEvents(stackName, stackEventsBucketKey);
      }
      computeStackCrupdateTriggeredEventProducer.accept(List.of(computeStackCrupdateTriggered));
    } else if (COMPUTE_STACK_DEPLOYED.equals(latestStatus) && stack.isPresent()) {
      stackCrupdatedEventProducer.accept(
          List.of(StackCrupdated.builder().userId(userId).stack(stack.get()).build()));
    }
  }

  private Optional<DeploymentState> getDeploymentLatestState(String appEnvDeploymentId) {
    Optional<DeploymentState> latestState;
    try {
      AppEnvironmentDeployment appEnvironmentDeployment =
          appEnvironmentDeploymentService.getById(appEnvDeploymentId);
      latestState = appEnvironmentDeployment.getLatestState();
    } catch (NotFoundException e) {
      latestState = Optional.empty();
    }
    return latestState;
  }
}
