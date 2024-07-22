package api.jcloudify.app.endpoint.rest.controller;

import static java.util.Objects.requireNonNull;

import api.jcloudify.app.endpoint.rest.mapper.EnvironmentMapper;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsResponse;
import api.jcloudify.app.endpoint.rest.model.EnvironmentsResponse;
import api.jcloudify.app.service.EnvironmentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ApplicationEnvironmentController {
  private final EnvironmentService service;
  private final EnvironmentMapper mapper;

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
}
