package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.repository.model.DeploymentState;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DeploymentStateMapper {
  private api.jcloudify.app.endpoint.rest.model.DeploymentState toRest(DeploymentState deploymentProgression) {
    return new api.jcloudify.app.endpoint.rest.model.DeploymentState()
        .id(deploymentProgression.getId())
        .executionType(deploymentProgression.getExecutionType())
        .progressionStatus(deploymentProgression.getProgressionStatus())
        .timestamp(deploymentProgression.getTimestamp());
  }

  public List<api.jcloudify.app.endpoint.rest.model.DeploymentState> toRest(
      List<DeploymentState> deploymentProgressions) {
    return deploymentProgressions.stream().map(this::toRest).toList();
  }
}
