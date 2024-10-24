package api.jcloudify.app.service.workflows;

import static api.jcloudify.app.endpoint.rest.model.ExecutionType.ASYNCHRONOUS;

import api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.DeploymentStateRepository;
import api.jcloudify.app.repository.model.DeploymentState;
import api.jcloudify.app.service.AppEnvironmentDeploymentService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DeploymentStateService {
  private final DeploymentStateRepository repository;
  private final AppEnvironmentDeploymentService appEnvironmentDeploymentService;

  public List<DeploymentState> getDeploymentStatesByDeploymentId(
      String userId,
      String appId,
      String deploymentId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    Pageable pageable = PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue());
    return repository.findAllByAppEnvDeploymentId(deploymentId, pageable);
  }

  public Optional<DeploymentState> getOptionalLatestDeploymentStateByDeploymentId(
      String appEnvDeploymentId) {
    return repository.findByAppEnvDeploymentId(appEnvDeploymentId);
  }

  public DeploymentState getLatestDeploymentStateByDeploymentId(String appEnvDeploymentId) {
    return getOptionalLatestDeploymentStateByDeploymentId(appEnvDeploymentId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "deployment state not found for appEnvDeploymentId: " + appEnvDeploymentId));
  }

  public void save(String appEnvDeploymentId, DeploymentStateEnum status) {
    var appEnvDepl = appEnvironmentDeploymentService.getById(appEnvDeploymentId);
    DeploymentState toBeAdded =
        DeploymentState.builder()
            .appEnvDeploymentId(appEnvDeploymentId)
            .progressionStatus(status)
            .executionType(ASYNCHRONOUS)
            .build();
    try {
      appEnvDepl.addState(toBeAdded);
      repository.save(toBeAdded);
    } catch (Exception e) {
      log.error("An error occurred when saving deployment state: {}", e.getMessage(), e);
    }
  }
}
