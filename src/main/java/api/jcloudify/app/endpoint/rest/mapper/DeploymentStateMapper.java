package api.jcloudify.app.endpoint.rest.mapper;

import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.*;

import api.jcloudify.app.endpoint.rest.model.DeploymentState;
import api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DeploymentStateMapper {
  public DeploymentState toRest(List<api.jcloudify.app.repository.model.DeploymentState> domains) {
    List<DeploymentState> restDeploymentStates = domains.stream().map(this::uniqueToRest).toList();
    DeploymentState result = new DeploymentState();
    if (!restDeploymentStates.isEmpty()) {
      if (restDeploymentStates.size() == 1) {
        return restDeploymentStates.getFirst();
      } else {
        for (int i = restDeploymentStates.size() - 1; i > 1; i--) {
          result = setNextState(restDeploymentStates.get(i - 1), restDeploymentStates.get(i));
        }
        return result;
      }
    }
    return result;
  }

  public DeploymentState uniqueToRest(api.jcloudify.app.repository.model.DeploymentState domain) {
    return new DeploymentState()
        .progressionStatus(domain.getProgressionStatus())
        .timestamp(domain.getTimestamp());
  }

  private DeploymentState setNextState(DeploymentState previousState, DeploymentState actualState) {
    DeploymentStateEnum actualStatus = actualState.getProgressionStatus();
    DeploymentStateEnum previousStatus = previousState.getProgressionStatus();

    if (previousState.getNextState() == null && actualStatus != null && previousStatus != null) {
      if (isValidTransition(previousStatus, actualStatus)) {
        previousState.setNextState(actualState);
        return previousState;
      }
      throw new IllegalArgumentException(
          String.format("Illegal transition status from=%s to=%s", previousStatus, actualStatus));
    }
    return previousState;
  }

  private boolean isValidTransition(
      DeploymentStateEnum previousStatus, DeploymentStateEnum actualStatus) {
    return switch (actualStatus) {
      case COMPUTE_STACK_DEPLOYED, COMPUTE_STACK_DEPLOYMENT_FAILED ->
          COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS.equals(previousStatus);
      case COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS ->
          INDEPENDENT_STACKS_DEPLOYMENT_FAILED.equals(previousStatus)
              || INDEPENDENT_STACKS_DEPLOYED.equals(previousStatus);
      case INDEPENDENT_STACKS_DEPLOYED, INDEPENDENT_STACKS_DEPLOYMENT_FAILED ->
          INDEPENDENT_STACKS_DEPLOYMENT_IN_PROGRESS.equals(previousStatus);
      case INDEPENDENT_STACKS_DEPLOYMENT_IN_PROGRESS ->
          INDEPENDENT_STACKS_DEPLOYMENT_INITIATED.equals(previousStatus);
      case INDEPENDENT_STACKS_DEPLOYMENT_INITIATED ->
          TEMPLATE_FILE_CHECK_FAILED.equals(previousStatus);
      case TEMPLATE_FILE_CHECK_FAILED -> TEMPLATE_FILE_CHECK_IN_PROGRESS.equals(previousStatus);
      default -> false;
    };
  }
}
