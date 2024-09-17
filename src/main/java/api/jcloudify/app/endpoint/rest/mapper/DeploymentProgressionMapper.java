package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.DeploymentProgressionEvent;
import api.jcloudify.app.repository.model.DeploymentProgression;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DeploymentProgressionMapper {
  private DeploymentProgressionEvent toRest(DeploymentProgression deploymentProgression) {
    return new DeploymentProgressionEvent()
        .id(deploymentProgression.getId())
        .executionType(deploymentProgression.getExecutionType())
        .progressionStatus(deploymentProgression.getProgressionStatus())
        .timestamp(deploymentProgression.getTimestamp());
  }

  public List<DeploymentProgressionEvent> toRest(
      List<DeploymentProgression> deploymentProgressions) {
    return deploymentProgressions.stream().map(this::toRest).toList();
  }
}
