package api.jcloudify.app.service;

import static api.jcloudify.app.service.StackService.setUpTags;

import api.jcloudify.app.aws.ssm.SsmComponent;
import api.jcloudify.app.endpoint.rest.mapper.SsmParameterMapper;
import api.jcloudify.app.endpoint.rest.model.SsmParameter;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.repository.jpa.SsmParameterRepository;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.model.Parameter;

@Service
@AllArgsConstructor
public class SsmParameterService {
  private final SsmParameterRepository repository;
  private final SsmComponent ssmComponent;
  private final ApplicationService applicationService;
  private final EnvironmentService environmentService;
  private final SsmParameterMapper mapper;

  public List<SsmParameter> crupdateParameters(
      String appId, String envId, List<SsmParameter> parameters) {
    Application application = applicationService.getById(appId);
    Environment environment = environmentService.getById(envId);
    Map<String, String> tags =
        setUpTags(application.getName(), environment.getEnvironmentType().toString().toLowerCase());
    List<Parameter> createdParameters = ssmComponent.crupdateSsmParameters(parameters, tags);
    List<api.jcloudify.app.repository.model.SsmParameter> saved =
        saveAll(mapper.toDomain(parameters, envId));
    return mapper.toRest(saved, createdParameters);
  }

  public Page<SsmParameter> findAll(
      String userId,
      String appId,
      String envId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    List<api.jcloudify.app.repository.model.SsmParameter> actualStored =
        findAllByCriteria(userId, appId, envId, pageFromOne, boundedPageSize);
    List<Parameter> actualWithValues =
        ssmComponent.getSsmParametersByNames(
            actualStored.stream()
                .map(api.jcloudify.app.repository.model.SsmParameter::getName)
                .toList());
    List<SsmParameter> result = mapper.toRest(actualStored, actualWithValues);
    return new Page<>(pageFromOne, boundedPageSize, result);
  }

  private List<api.jcloudify.app.repository.model.SsmParameter> saveAll(
      List<api.jcloudify.app.repository.model.SsmParameter> parameters) {
    return repository.saveAll(parameters);
  }

  private List<api.jcloudify.app.repository.model.SsmParameter> findAllByCriteria(
      String userId,
      String appId,
      String envId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    Pageable pageable = PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue());
    return repository.findByCriteria(userId, appId, envId, pageable).toList();
  }
}
