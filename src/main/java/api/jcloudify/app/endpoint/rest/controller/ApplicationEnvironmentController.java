package api.jcloudify.app.endpoint.rest.controller;

import static java.util.Objects.requireNonNull;

import api.jcloudify.app.endpoint.rest.mapper.EnvironmentMapper;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentSsmParameters;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsResponse;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.EnvironmentsResponse;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.SsmParameter;
import api.jcloudify.app.endpoint.validator.CrupdateEnvironmentSsmParametersValidator;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.SsmParameterService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class ApplicationEnvironmentController {
  private final EnvironmentService service;
  private final EnvironmentMapper mapper;
  private final SsmParameterService ssmParameterService;
  private final CrupdateEnvironmentSsmParametersValidator crupdateEnvironmentSsmParametersValidator;

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

  @PutMapping("/users/{userId}/applications/{applicationId}/environments/{environmentId}/ssmparameters")
  public CrupdateEnvironmentSsmParameters crupdateEnvironmentSsmParameters(
          @PathVariable String userId,
          @PathVariable String applicationId,
          @PathVariable String environmentId,
          @RequestBody CrupdateEnvironmentSsmParameters requestBody) {
    crupdateEnvironmentSsmParametersValidator.accept(requestBody);
    List<SsmParameter> crupdatedSsmParameters = ssmParameterService.crupdateParameters(applicationId, environmentId, requestBody.getData());
    return new CrupdateEnvironmentSsmParameters().data(crupdatedSsmParameters);
  }
}
