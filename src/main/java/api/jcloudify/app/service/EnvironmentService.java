package api.jcloudify.app.service;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvironmentRepository;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.appEnvConfigurer.AppEnvConfigurerService;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {
  private final EnvironmentRepository repository;
  private final AppEnvConfigurerService configurerService;
  private final StackService stackService;

  public EnvironmentService(
      EnvironmentRepository repository,
      AppEnvConfigurerService configurerService,
      @Lazy StackService stackService) {
    this.repository = repository;
    this.configurerService = configurerService;
    this.stackService = stackService;
  }

  public List<Environment> findAllByApplicationId(String applicationId) {
    return repository.findAllByApplicationId(applicationId);
  }

  public Environment getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("Environment identified by id " + id + " not found"));
  }

  private void archiveEnvironment(Environment environment) {
    List<Stack> stacksToArchive =
        stackService.findAllByEnvId(environment.getId()).stream()
            .filter(stack -> !stack.isArchived())
            .toList();
    stacksToArchive.forEach(stackService::deleteAndArchiveStack);
  }

  public List<Environment> crupdateEnvironments(
      String applicationId, List<Environment> environments) {
    environments.forEach(
        environment ->
            checkIfEnvironmentExists(
                environment.getId(), applicationId, environment.getEnvironmentType()));
    List<Environment> toArchive = environments.stream().filter(Environment::isArchived).toList();
    if (!toArchive.isEmpty()) {
      toArchive.forEach(this::archiveEnvironment);
    }
    return repository.saveAll(environments);
  }

  public final OneOfPojaConf configureEnvironment(
      String userId, String appId, String environmentId, OneOfPojaConf pojaConf) {
    return configurerService.configureEnvironment(userId, appId, environmentId, pojaConf);
  }

  public final OneOfPojaConf getConfig(String userId, String appId, String environmentId) {
    Environment linkedEnvironment = getById(environmentId);
    String configurationFileKey = linkedEnvironment.getConfigurationFileKey();
    if (configurationFileKey == null) {
      throw new ApiException(
          SERVER_EXCEPTION,
          "config not found in DB for user.Id = "
              + userId
              + " app.Id = "
              + appId
              + " environment.Id = "
              + environmentId);
    }
    return configurerService.readConfig(userId, appId, environmentId, configurationFileKey);
  }

  public void checkIfEnvironmentExists(String id, String appId, EnvironmentType type) {
    Optional<Environment> actualById = repository.findById(id);
    if (actualById.isEmpty()) {
      Optional<Environment> actualByAppIdAndType =
          repository.findFirstByApplicationIdAndEnvironmentType(appId, type);
      if (actualByAppIdAndType.isPresent()) {
        throw new BadRequestException("Only one " + type + " environment can be created.");
      }
    }
  }

  public Environment getUserApplicationEnvironmentById(
      String userId, String applicationId, String environmentId) {
    return repository
        .findByCriteria(userId, applicationId, environmentId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Environment identified by id "
                        + environmentId
                        + " for application "
                        + applicationId
                        + " of user "
                        + userId
                        + " not found"));
  }

  public Environment getUserApplicationEnvironmentByIdAndType(
      String userId, String applicationId, EnvironmentType environmentType) {
    return repository
        .findByCriteria(userId, applicationId, environmentType)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Environment identified by type "
                        + environmentType
                        + " for application "
                        + applicationId
                        + " of user "
                        + userId
                        + " not found"));
  }
}
