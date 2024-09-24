package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_FAILED;
import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_SUCCESS;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.COMPUTE_STACK_DEPLOYED;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.COMPUTE_STACK_DEPLOYMENT_FAILED;

import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdated;
import api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus;
import api.jcloudify.app.service.workflows.DeploymentStateService;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ComputeStackCrupdatedService implements Consumer<ComputeStackCrupdated> {
  private final DeploymentStateService deploymentStateService;

  @Override
  public void accept(ComputeStackCrupdated computeStackCrupdated) {
    String appEnvDeploymentId = computeStackCrupdated.getAppEnvDeploymentId();
    StackCrupdateStatus stackDeploymentState = computeStackCrupdated.getStackDeploymentState();

    if (stackDeploymentState.equals(CRUPDATE_SUCCESS)) {
      deploymentStateService.save(appEnvDeploymentId, COMPUTE_STACK_DEPLOYED);
    } else if (stackDeploymentState.equals(CRUPDATE_FAILED)) {
      deploymentStateService.save(appEnvDeploymentId, COMPUTE_STACK_DEPLOYMENT_FAILED);
    }
  }
}
