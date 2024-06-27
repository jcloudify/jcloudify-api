package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.ApplicationMapper;
import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.StackService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ApplicationController {
  private final StackService stackService;
  private final ApplicationService applicationService;

  @Qualifier("RestApplicationMapper")
  private final ApplicationMapper mapper;

  @PutMapping("/applications")
  public List<Application> crupdateApplications(@RequestBody List<ApplicationBase> toCrupdate) {
    return mapper.toRest(applicationService.saveApplications(toCrupdate));
  }

  @PostMapping("/applications/{applicationId}/environments/{environmentId}/deploymentInitiation")
  public List<Stack> deployStack(
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestBody List<InitiateDeployment> deploymentsToInitiate) {
    return stackService.process(deploymentsToInitiate, applicationId, environmentId);
  }
}
