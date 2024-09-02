package api.jcloudify.app.endpoint.rest.controller;

import static java.util.Objects.requireNonNull;

import api.jcloudify.app.endpoint.rest.mapper.EnvironmentMapper;
import api.jcloudify.app.endpoint.rest.model.CreateEnvironmentSsmParameters;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentSsmParametersResponse;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsResponse;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.EnvironmentsResponse;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PagedEnvironmentSsmParameters;
import api.jcloudify.app.endpoint.rest.model.PagedLogGroups;
import api.jcloudify.app.endpoint.rest.model.PagedLogStreams;
import api.jcloudify.app.endpoint.rest.model.SsmParameter;
import api.jcloudify.app.endpoint.rest.model.UpdateEnvironmentSsmParameters;
import api.jcloudify.app.endpoint.validator.CreateEnvironmentSsmParametersValidator;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.LambdaFunctionLogService;
import api.jcloudify.app.service.SsmParameterService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ApplicationEnvironmentController {
  private final EnvironmentService service;
  private final EnvironmentMapper mapper;
  private final SsmParameterService ssmParameterService;
  private final CreateEnvironmentSsmParametersValidator createEnvironmentSsmParametersValidator;
  private final LambdaFunctionLogService lambdaFunctionLogService;

  @GetMapping("/users/{userId}/applications/{applicationId}/environments")
  public EnvironmentsResponse getApplicationEnvironments(
      @PathVariable String userId, @PathVariable String applicationId) {
    var data = service.findAllByApplicationId(applicationId).stream().map(mapper::toRest).toList();
    return new EnvironmentsResponse().data(data);
  }

  @PutMapping("/users/{userId}/applications/{applicationId}/environments")
  public CrupdateEnvironmentsResponse crupdateApplicationEnvironments(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @RequestBody CrupdateEnvironmentsRequestBody requestBody) {
    var requestBodyData = requireNonNull(requestBody.getData());
    var mappedData = requestBodyData.stream().map(a -> mapper.toDomain(applicationId, a)).toList();
    var data =
        service.crupdateEnvironments(applicationId, mappedData).stream()
            .map(mapper::toRest)
            .toList();
    return new CrupdateEnvironmentsResponse().data(data);
  }

  @PutMapping("/users/{userId}/applications/{applicationId}/environments/{environmentId}/config")
  public OneOfPojaConf configureApplicationEnv(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestBody OneOfPojaConf requestBody) {
    return service.configureEnvironment(userId, applicationId, environmentId, requestBody);
  }

  @GetMapping("/users/{userId}/applications/{applicationId}/environments/{environmentId}/config")
  public OneOfPojaConf getApplicationEnvironmentConfig(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId) {
    return service.getConfig(userId, applicationId, environmentId);
  }

  @GetMapping("/users/{userId}/applications/{applicationId}/environments/{environmentId}")
  public Environment getApplicationEnvironmentById(
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @PathVariable String userId) {
    return mapper.toRest(
        service.getUserApplicationEnvironmentById(userId, applicationId, environmentId));
  }

  @PostMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/ssmparameters")
  public CrupdateEnvironmentSsmParametersResponse createEnvironmentSsmParameters(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestBody CreateEnvironmentSsmParameters requestBody) {
    createEnvironmentSsmParametersValidator.accept(requestBody);
    List<SsmParameter> createdSsmParameters =
        ssmParameterService.createParameters(applicationId, environmentId, requestBody.getData());
    return new CrupdateEnvironmentSsmParametersResponse().data(createdSsmParameters);
  }

  @PutMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/ssmparameters")
  public CrupdateEnvironmentSsmParametersResponse updateEnvironmentSsmParameters(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestBody UpdateEnvironmentSsmParameters requestBody) {
    List<SsmParameter> createdSsmParameters =
        ssmParameterService.updateParameters(applicationId, environmentId, requestBody.getData());
    return new CrupdateEnvironmentSsmParametersResponse().data(createdSsmParameters);
  }

  @GetMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/ssmparameters")
  public PagedEnvironmentSsmParameters getSsmParameters(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize) {
    var data = ssmParameterService.findAll(userId, applicationId, environmentId, page, pageSize);
    var responseData = data.data().stream().toList();
    return new PagedEnvironmentSsmParameters()
        .count(data.count())
        .hasPrevious(data.hasPrevious())
        .pageSize(data.queryPageSize().getValue())
        .pageNumber(data.queryPage().getValue())
        .data(responseData);
  }

  @GetMapping(
      "/users/{userId}/applications/{applicationId}/environments/{environmentId}/functions/{functionName}/logGroups")
  private PagedLogGroups getFunctionLogGroups(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @PathVariable String environmentId,
      @PathVariable String functionName,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize) {
    var data =
        lambdaFunctionLogService.getLogGroups(
            userId, applicationId, environmentId, functionName, page, pageSize);
    var responseData = data.data().stream().toList();
    return new PagedLogGroups()
        .data(responseData)
        .count(data.count())
        .pageSize(data.queryPageSize().getValue())
        .pageNumber(data.queryPage().getValue())
        .hasPrevious(data.hasPrevious());
  }

  @GetMapping(
          "/users/{userId}/applications/{applicationId}/environments/{environmentId}/functions/{functionName}/logGroups/{logGroupName}/logStreams")
  private PagedLogStreams getLogStreams(
          @PathVariable String userId,
          @PathVariable String applicationId,
          @PathVariable String environmentId,
          @PathVariable String functionName,
          @PathVariable String logGroupName,
          @RequestParam(required = false, defaultValue = "1") PageFromOne page,
          @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize) {
    var data =
            lambdaFunctionLogService.getLogStreams(
                    userId, applicationId, environmentId, functionName, logGroupName, page, pageSize);
    var responseData = data.data().stream().toList();
    return new PagedLogStreams()
            .data(responseData)
            .count(data.count())
            .pageSize(data.queryPageSize().getValue())
            .pageNumber(data.queryPage().getValue())
            .hasPrevious(data.hasPrevious());
  }
}
