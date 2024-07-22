package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentRequestBody;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentResponse;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.service.StackService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@AllArgsConstructor
public class StackController {
    private final StackService service;

    @GetMapping("/applications/{applicationId}/environments/{environmentId}/stacks/{stackId}")
    public Stack getStacks(@PathVariable String applicationId,
                           @PathVariable String environmentId,
                           @PathVariable String stackId) {
        return service.getById(stackId);
    }

    @PostMapping("/applications/{applicationId}/environments/{environmentId}/deploymentInitiation")
    public InitiateStackDeploymentResponse initiatedStackDeployment(
            @PathVariable String applicationId,
            @PathVariable String environmentId,
            @RequestBody InitiateStackDeploymentRequestBody deploymentsToInitiate) {
        var data =
                service.process(
                        Objects.requireNonNull(deploymentsToInitiate.getData()), applicationId, environmentId);
        return new InitiateStackDeploymentResponse().data(data);
    }
}
