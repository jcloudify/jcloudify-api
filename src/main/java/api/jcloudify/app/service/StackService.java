package api.jcloudify.app.service;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.aws.cloudformation.CloudformationTemplateConf;
import api.jcloudify.app.endpoint.rest.mapper.StackMapper;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.repository.jpa.StackRepository;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.Stack;
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

  private Optional<Stack> checkIfStackAlreadyExists(
      String applicationId, String environmentId, StackType type) {
    return repository.findByApplicationIdAndEnvironmentIdAndType(
        applicationId, environmentId, type);
  }

  private Stack save(
      String name, String cfStackId, String applicationId, String environmentId, StackType type) {
    return repository.save(
        Stack.builder()
            .name(name)
            .cfStackId(cfStackId)
            .applicationId(applicationId)
            .environmentId(environmentId)
            .type(type)
            .build());
  }

  private api.jcloudify.app.endpoint.rest.model.Stack deployStack(
      InitiateDeployment toDeploy, String applicationId, String environmentId) {
    Optional<Stack> stack =
        checkIfStackAlreadyExists(applicationId, environmentId, toDeploy.getStackType());
    Environment environment = environmentService.getById(environmentId);
    String environmentType = environment.getEnvironmentType().toString().toLowerCase();
    Application application = applicationService.getById(applicationId);
    String applicationName = application.getName().replace("_", "-");
    Map<String, String> parameters = new HashMap<>();
    parameters.put("Env", environmentType);
    parameters.put("AppName", applicationName);
    if (stack.isPresent()) {
      Stack toUpdate = stack.get();
      Map<String, String> tags = setUpTags(toUpdate.getName(), environmentType);
      String cfStackId = updateStack(toDeploy, parameters, toUpdate.getName(), tags);
      return mapper.toRest(
          save(toUpdate.getName(), cfStackId, applicationId, environmentId, toUpdate.getType()),
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
          save(stackName, cfStackId, applicationId, environmentId, toDeploy.getStackType()),
          application,
          environment);
    }
  }

  private String createStack(
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      String stackName,
      Map<String, String> tags) {
    String stackId;
    switch (toDeploy.getStackType()) {
      case EVENT -> {
        parameters.put("Prefix", "1");
        stackId =
            cloudformationComponent.createStack(
                stackName,
                cloudformationTemplateConf.getEventStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case COMPUTE_PERMISSION -> {
        stackId =
            cloudformationComponent.createStack(
                stackName,
                cloudformationTemplateConf.getComputePermissionStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case STORAGE_BUCKET -> {
        stackId =
            cloudformationComponent.createStack(
                stackName,
                cloudformationTemplateConf.getStorageBucketStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case STORAGE_DATABASE -> {
        stackId =
            cloudformationComponent.createStack(
                stackName,
                cloudformationTemplateConf.getStorageDatabaseStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case null -> throw new BadRequestException("Stack type to deploy must be defined");
    }
    return stackId;
  }

  private String updateStack(
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      String stackName,
      Map<String, String> tags) {
    String stackId;
    switch (toDeploy.getStackType()) {
      case EVENT -> {
        parameters.put("Prefix", "1");
        stackId =
            cloudformationComponent.updateStack(
                stackName,
                cloudformationTemplateConf.getEventStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case COMPUTE_PERMISSION -> {
        stackId =
            cloudformationComponent.updateStack(
                stackName,
                cloudformationTemplateConf.getComputePermissionStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case STORAGE_BUCKET -> {
        stackId =
            cloudformationComponent.updateStack(
                stackName,
                cloudformationTemplateConf.getStorageBucketStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case STORAGE_DATABASE -> {
        stackId =
            cloudformationComponent.updateStack(
                stackName,
                cloudformationTemplateConf.getStorageDatabaseStackTemplateUrl().toString(),
                parameters,
                tags);
      }
      case null -> throw new BadRequestException("Stack type to deploy must be defined");
    }
    return stackId;
  }

  private static Map<String, String> setUpTags(String applicationName, String applicationEnv) {
    Map<String, String> tags = new HashMap<>();
    tags.put("app", applicationName);
    tags.put("env", applicationEnv);
    tags.put("user:poja", applicationName);
    return tags;
  }
}
