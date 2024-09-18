package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.AppEnvDeploymentsRestMapper;
import api.jcloudify.app.endpoint.rest.model.AppEnvDeployment;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PagedDeploymentStates;
import api.jcloudify.app.endpoint.rest.model.PagedDeploymentsResponse;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.service.AppEnvironmentDeploymentService;
import api.jcloudify.app.service.workflows.DeploymentStateService;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ApplicationDeploymentsController {
  private final AppEnvironmentDeploymentService appEnvironmentDeploymentService;
  private final AppEnvDeploymentsRestMapper mapper;
  private final DeploymentStateService deploymentStateService;

  @GetMapping("/users/{userId}/applications/{applicationId}/deployments")
  public PagedDeploymentsResponse getApplicationDeployments(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @RequestParam(required = false) Instant startDatetime,
      @RequestParam(required = false) Instant endDatetime,
      @RequestParam(required = false) EnvironmentType environmentType,
      @RequestParam(required = false) PageFromOne page,
      @RequestParam(required = false, name = "page_size") BoundedPageSize pageSize) {
    var pagedResults =
        appEnvironmentDeploymentService.findAllByCriteria(
            userId, applicationId, environmentType, startDatetime, endDatetime, page, pageSize);
    List<AppEnvDeployment> data = pagedResults.data().stream().map(mapper::toRest).toList();

    return new PagedDeploymentsResponse()
        .count(pagedResults.count())
        .hasPrevious(pagedResults.hasPrevious())
        .data(data);
  }

  @GetMapping("/users/{userId}/applications/{applicationId}/deployments/{deploymentId}")
  public AppEnvDeployment getApplicationDeployment(@PathVariable String deploymentId) {
    return mapper.toRest(appEnvironmentDeploymentService.getById(deploymentId));
  }

  @GetMapping("/users/{userId}/applications/{applicationId}/deployments/{deploymentId}/config")
  public OneOfPojaConf getApplicationDeploymentConfig(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String deploymentId) {
    return appEnvironmentDeploymentService.getConfig(userId, applicationId, deploymentId);
  }

  @GetMapping("/users/{userId}/applications/{applicationId}/deployments/{deploymentId}/progression")
  public PagedDeploymentStates getApplicationDeploymentProgression(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String deploymentId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize) {
    var pagedResults =
        deploymentStateService.getDeploymentStatesByDeploymentId(
            userId, applicationId, deploymentId, page, pageSize);
    return new PagedDeploymentStates()
        .count(pagedResults.count())
        .hasPrevious(pagedResults.hasPrevious())
        .pageSize(pagedResults.queryPageSize().getValue())
        .pageNumber(pagedResults.queryPage().getValue())
        .data(pagedResults.data().stream().toList());
  }
}
