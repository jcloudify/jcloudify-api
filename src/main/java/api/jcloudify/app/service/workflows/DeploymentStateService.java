package api.jcloudify.app.service.workflows;

import static api.jcloudify.app.endpoint.rest.model.ExecutionType.ASYNCHRONOUS;

import api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.repository.jpa.DeploymentStateRepository;
import api.jcloudify.app.repository.model.DeploymentState;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeploymentStateService {
  private final DeploymentStateRepository repository;

  public List<DeploymentState> getDeploymentStatesByDeploymentId(
      String userId,
      String appId,
      String deploymentId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    Pageable pageable = PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue());
    return repository.findAllByAppEnvDeploymentId(deploymentId, pageable);
  }

  public void save(String appEnvDeploymentId, DeploymentStateEnum status) {
    repository.save(
        DeploymentState.builder()
            .appEnvDeploymentId(appEnvDeploymentId)
            .progressionStatus(status)
            .executionType(ASYNCHRONOUS)
            .build());
  }
}
