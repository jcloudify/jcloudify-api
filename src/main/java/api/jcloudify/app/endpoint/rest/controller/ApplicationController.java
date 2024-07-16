package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.ApplicationMapper;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsResponse;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentRequestBody;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentResponse;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.StackService;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    var data =
        mapper.toRest(
            applicationService.saveApplications(Objects.requireNonNull(toCrupdate.getData())));
    return new CrupdateApplicationsResponse().data(data);
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
}
