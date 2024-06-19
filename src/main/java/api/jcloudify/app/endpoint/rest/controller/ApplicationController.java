package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.DeploymentInitiated;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.service.DeploymentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class ApplicationController {
    private final DeploymentService service;

    @PostMapping("/applications/{applicationId}/environments/{environmentId}/deploymentInitiation")
    public List<DeploymentInitiated> deployStack(@PathVariable String applicationId, @PathVariable String environmentId,
                                                 @RequestBody List<InitiateDeployment> deploymentsToInitiate) {
        return service.process(deploymentsToInitiate);
    }
}
