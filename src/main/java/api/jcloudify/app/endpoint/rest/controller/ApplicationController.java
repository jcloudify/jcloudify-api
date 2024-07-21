package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.ApplicationMapper;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsResponse;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentRequestBody;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentResponse;
import api.jcloudify.app.endpoint.rest.model.PagedApplicationsResponse;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.StackService;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {
  private final StackService stackService;
  private final ApplicationService applicationService;
  private final ApplicationMapper mapper;

  public ApplicationController(
      StackService stackService,
      ApplicationService applicationService,
      @Qualifier("RestApplicationMapper") ApplicationMapper mapper) {
    this.stackService = stackService;
    this.applicationService = applicationService;
    this.mapper = mapper;
  }

  @PutMapping("/applications")
  public CrupdateApplicationsResponse crupdateApplications(
      @RequestBody CrupdateApplicationsRequestBody toCrupdate) {
    var data = Objects.requireNonNull(toCrupdate.getData());
    var mappedData =
        applicationService.saveApplications(data).stream().map(mapper::toRest).toList();
    return new CrupdateApplicationsResponse().data(mappedData);
  }

  @PostMapping("/applications/{applicationId}/environments/{environmentId}/deploymentInitiation")
  public InitiateStackDeploymentResponse initiatedStackDeployment(
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestBody InitiateStackDeploymentRequestBody deploymentsToInitiate) {
    var data =
        stackService.process(
            Objects.requireNonNull(deploymentsToInitiate.getData()), applicationId, environmentId);
    return new InitiateStackDeploymentResponse().data(data);
  }

  @GetMapping("/applications")
  public PagedApplicationsResponse getApplications(
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize,
      @RequestParam(name = "user_id") String userId,
      @RequestParam(required = false) String name) {
    var pagedData = applicationService.findAllByCriteria(userId, name, page, pageSize);
    var mappedData = pagedData.data().stream().map(mapper::toRest).toList();
    return new PagedApplicationsResponse()
        .count(pagedData.count())
        .hasPrevious(pagedData.hasPrevious())
        .pageSize(pagedData.queryPageSize().getValue())
        .pageNumber(pagedData.queryPage().getValue())
        .data(mappedData);
  }
}
