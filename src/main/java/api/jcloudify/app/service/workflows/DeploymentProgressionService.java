package api.jcloudify.app.service.workflows;

import api.jcloudify.app.endpoint.rest.mapper.DeploymentProgressionMapper;
import api.jcloudify.app.endpoint.rest.model.DeploymentProgressionEvent;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.repository.jpa.DeploymentProgressionRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeploymentProgressionService {
  private final DeploymentProgressionMapper mapper;
  private final DeploymentProgressionRepository repository;

  public Page<DeploymentProgressionEvent> getProgressionsByDeploymentId(
      String userId,
      String appId,
      String deploymentId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    Pageable pageable = PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue());
    var data = repository.findAllByAppEnvDeploymentId(deploymentId, pageable);
    return new Page<>(pageFromOne, boundedPageSize, mapper.toRest(data));
  }
}
