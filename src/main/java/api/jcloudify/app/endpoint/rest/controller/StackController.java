package api.jcloudify.app.endpoint.rest.controller;

import static java.util.Objects.requireNonNull;

import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentRequestBody;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentResponse;
import api.jcloudify.app.endpoint.rest.model.PagedStackEvents;
import api.jcloudify.app.endpoint.rest.model.PagedStackOutputs;
import api.jcloudify.app.endpoint.rest.model.PagedStacksResponse;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.service.StackService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class StackController {
  private final StackService service;

  @GetMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/stacks/{stackId}")
  public Stack getStacks(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @PathVariable String stackId) {
    return service.getById(userId, applicationId, environmentId, stackId);
  }

  @GetMapping("/users/{userId}/applications/{applicationId}/environments/{environmentId}/stacks")
  public PagedStacksResponse getStacks(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize) {
    var data = service.findAllBy(userId, applicationId, environmentId, page, pageSize);
    var responseData = data.data().stream().toList();
    return new PagedStacksResponse()
        .count(data.count())
        .hasPrevious(data.hasPrevious())
        .pageSize(data.queryPageSize().getValue())
        .pageNumber(data.queryPage().getValue())
        .data(responseData);
  }

  @GetMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/stacks/{stackId}/events")
  public PagedStackEvents getStackEvents(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @PathVariable String stackId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize) {
    var data =
        service.getStackEvents(userId, applicationId, environmentId, stackId, page, pageSize);
    var responseData = data.data().stream().toList();
    return new PagedStackEvents()
        .count(data.count())
        .hasPrevious(data.hasPrevious())
        .pageSize(data.queryPageSize().getValue())
        .pageNumber(data.queryPage().getValue())
        .data(responseData);
  }

  @GetMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/stacks/{stackId}/outputs")
  public PagedStackOutputs getStackOutputs(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @PathVariable String stackId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize) {
    var data =
        service.getStackOutputs(userId, applicationId, environmentId, stackId, page, pageSize);
    var responseData = data.data().stream().toList();
    return new PagedStackOutputs()
        .count(data.count())
        .hasPrevious(data.hasPrevious())
        .pageSize(data.queryPageSize().getValue())
        .pageNumber(data.queryPage().getValue())
        .data(responseData);
  }

  @PutMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/deploymentInitiation")
  public InitiateStackDeploymentResponse initiatedStackDeployment(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestBody InitiateStackDeploymentRequestBody deploymentsToInitiate) {
    var data =
        service.process(
            requireNonNull(deploymentsToInitiate.getData()), userId, applicationId, environmentId);
    return new InitiateStackDeploymentResponse().data(data);
  }
}
