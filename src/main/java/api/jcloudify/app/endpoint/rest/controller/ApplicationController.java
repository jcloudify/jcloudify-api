package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.EnvironmentMapper;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironment;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.service.EnvironmentService;
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
    private final EnvironmentService environmentService;
    private final StackService stackService;
    @Qualifier("RestEnvironmentMapper")
    private final EnvironmentMapper mapper;

    @PostMapping("/applications/{applicationId}/environments/{environmentId}/deploymentInitiation")
    public List<Stack> deployStack(
            @PathVariable String applicationId,
            @PathVariable String environmentId,
            @RequestBody List<InitiateDeployment> deploymentsToInitiate) {
        return stackService.process(deploymentsToInitiate, applicationId, environmentId);
    }

    @PutMapping("/applications/{applicationId}/environments")
    public List<Environment> crupdateEnvironments(@PathVariable String applicationId, @RequestBody List<CrupdateEnvironment> toSave) {
        return environmentService.saveEnvironments(applicationId, toSave).stream().map(mapper::toRest).toList();
    }
}
