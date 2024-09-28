package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_FAILED;
import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_SUCCESS;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.COMPUTE_STACK_DEPLOYED;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.COMPUTE_STACK_DEPLOYMENT_FAILED;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdated;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus;
import api.jcloudify.app.repository.jpa.dao.StackDao;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.workflows.DeploymentStateService;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ComputeStackCrupdatedService implements Consumer<ComputeStackCrupdated> {
  private final DeploymentStateService deploymentStateService;
  private final StackDao stackDao;
  private final EventProducer<StackCrupdated> stackCrupdatedEventProducer;

  @Override
  public void accept(ComputeStackCrupdated computeStackCrupdated) {
    String appEnvDeploymentId = computeStackCrupdated.getAppEnvDeploymentId();
    StackCrupdateStatus stackDeploymentState = computeStackCrupdated.getStackDeploymentState();
    String userId = computeStackCrupdated.getUserId();
    Optional<Stack> stack =
        stackDao.findByCriteria(
            computeStackCrupdated.getAppId(), computeStackCrupdated.getEnvId(), COMPUTE);

    if (CRUPDATE_SUCCESS.equals(stackDeploymentState) && stack.isPresent()) {
      deploymentStateService.save(appEnvDeploymentId, COMPUTE_STACK_DEPLOYED);
      stackCrupdatedEventProducer.accept(
          List.of(StackCrupdated.builder().userId(userId).stack(stack.get()).build()));
    } else if (CRUPDATE_FAILED.equals(stackDeploymentState)) {
      deploymentStateService.save(appEnvDeploymentId, COMPUTE_STACK_DEPLOYMENT_FAILED);
    }
  }
}
