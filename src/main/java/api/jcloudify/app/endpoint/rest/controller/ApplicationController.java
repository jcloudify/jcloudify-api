package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.service.StackService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ApplicationController {
  private final StackService service;

  @PostMapping("/applications/{applicationId}/environments/{environmentId}/deploymentInitiation")
  public List<Stack> deployStack(
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestBody List<InitiateDeployment> deploymentsToInitiate) {
    return service.process(deploymentsToInitiate, applicationId, environmentId);
  }
}
