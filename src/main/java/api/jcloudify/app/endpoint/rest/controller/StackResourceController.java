package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.ComputeStackResourceMapper;
import api.jcloudify.app.endpoint.rest.model.ComputeStackResource;
import api.jcloudify.app.endpoint.rest.model.PagedComputeStackResource;
import api.jcloudify.app.service.ComputeStackResourceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class StackResourceController {
    private final ComputeStackResourceService computeStackResourceService;
    private final ComputeStackResourceMapper computeStackResourceMapper;

    @GetMapping("/users/{userId}/applications/{applicationId}/environments/{environmentId}/computeStackResources")
    public PagedComputeStackResource getComputeStackResources(
            @PathVariable String userId, @PathVariable String applicationId, @PathVariable String environmentId) {
        List<ComputeStackResource> computeStackResources = computeStackResourceService.findAllByEnvironmentId(environmentId)
                .stream().map(computeStackResourceMapper::toRest)
                .toList();
        return new PagedComputeStackResource().data(computeStackResources);
    }
}
