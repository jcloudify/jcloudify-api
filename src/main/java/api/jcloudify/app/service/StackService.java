package api.jcloudify.app.service;

import static java.util.Objects.requireNonNull;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.aws.cloudformation.CloudformationTemplateConf;
import api.jcloudify.app.endpoint.rest.mapper.StackMapper;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.StackRepository;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.Stack;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class StackService {
  private final CloudformationTemplateConf cloudformationTemplateConf;
  private final CloudformationComponent cloudformationComponent;
  private final EnvironmentService environmentService;
  private final ApplicationService applicationService;
  private final StackRepository repository;
  private final StackMapper mapper;

  public List<api.jcloudify.app.endpoint.rest.model.Stack> process(
      List<InitiateDeployment> deployments, String applicationId, String environmentId) {
    return deployments.stream()
        .map(stack -> this.deployStack(stack, applicationId, environmentId))
        .toList();
  }

  private Optional<Stack> findBy(String applicationId, String environmentId, StackType type) {
    return repository.findByApplicationIdAndEnvironmentIdAndType(
        applicationId, environmentId, type);
  }

  public List<api.jcloudify.app.endpoint.rest.model.Stack> findAllBy(String applicationId, String environmentId) {
    return repository.findAllByApplicationIdAndEnvironmentId(applicationId, environmentId).stream()
            .map(this::toRestWithApplicationAndEnvironment)
            .toList();
  }

  public api.jcloudify.app.endpoint.rest.model.Stack getById(String stackId) {
    return toRestWithApplicationAndEnvironment(repository.findById(stackId)
            .orElseThrow(() -> new NotFoundException("Stack id=" + stackId +" not found")));
  }

  private api.jcloudify.app.endpoint.rest.model.Stack toRestWithApplicationAndEnvironment(Stack domain) {
    Application application = applicationService.getById(domain.getApplicationId());
    Environment environment = environmentService.getById(domain.getEnvironmentId());
    return mapper.toRest(domain, application, environment);
  }

  private Stack save(Stack toSave) {
    return repository.save(toSave);
  }

  private api.jcloudify.app.endpoint.rest.model.Stack deployStack(
      InitiateDeployment toDeploy, String applicationId, String environmentId) {
    Application application = applicationService.getById(applicationId);
    Environment environment = environmentService.getById(environmentId);
    String environmentType = environment.getFormattedEnvironmentType();
    String applicationName = application.getFormattedName();
    Map<String, String> parameters = getParametersFrom(environmentType, applicationName);

    Optional<Stack> stack = findBy(applicationId, environmentId, toDeploy.getStackType());
    if (stack.isPresent()) {
      Stack toUpdate = stack.get();
      Map<String, String> tags = setUpTags(toUpdate.getName(), environmentType);
      String cfStackId = updateStack(toDeploy, parameters, toUpdate.getName(), tags);
      return mapper.toRest(
          save(
              Stack.builder()
                  .name(toUpdate.getName())
                  .cfStackId(cfStackId)
                  .applicationId(applicationId)
                  .environmentId(environmentId)
                  .type(toUpdate.getType())
                  .creationDatetime(Instant.now())
                  .build()),
          application,
          environment);
    } else {
      String stackName =
          String.format(
              "%s-%s-%s",
              environmentType,
              String.valueOf(toDeploy.getStackType()).toLowerCase().replace("_", "-"),
              applicationName);
      Map<String, String> tags = setUpTags(stackName, environmentType);
      String cfStackId = createStack(toDeploy, parameters, stackName, tags);
      return mapper.toRest(
          save(
              Stack.builder()
                  .name(stackName)
                  .cfStackId(cfStackId)
                  .applicationId(applicationId)
                  .environmentId(environmentId)
                  .type(toDeploy.getStackType())
                  .creationDatetime(Instant.now())
                  .build()),
          application,
          environment);
    }
  }

  private static Map<String, String> getParametersFrom(
      String environmentType, String applicationName) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("Env", environmentType);
    parameters.put("AppName", applicationName);
    return parameters;
  }

  private String getStackTemplateUrlFrom(StackType stackType) {
    return (switch (stackType) {
          case EVENT -> cloudformationTemplateConf.getEventStackTemplateUrl();
          case COMPUTE_PERMISSION -> cloudformationTemplateConf
              .getComputePermissionStackTemplateUrl();
          case STORAGE_BUCKET -> cloudformationTemplateConf.getStorageBucketStackTemplateUrl();
          case STORAGE_DATABASE_POSTGRES -> cloudformationTemplateConf
              .getStorageDatabasePostgresStackTemplateUrl();
          case STORAGE_DATABASE_SQLITE -> cloudformationTemplateConf
              .getStorageDatabaseSQliteStackTemplateUrl();
        })
        .toString();
  }

  private String createStack(
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.createStack(
        stackName,
        getStackTemplateUrlFrom(requireNonNull(toDeploy.getStackType())),
        parameters,
        tags);
  }

  private String updateStack(
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.updateStack(
        stackName,
        getStackTemplateUrlFrom(requireNonNull(toDeploy.getStackType())),
        parameters,
        tags);
  }

  private static Map<String, String> setUpTags(String applicationName, String applicationEnv) {
    Map<String, String> tags = new HashMap<>();
    tags.put("app", applicationName);
    tags.put("env", applicationEnv);
    tags.put("user:poja", applicationName);
    return tags;
  }
}
