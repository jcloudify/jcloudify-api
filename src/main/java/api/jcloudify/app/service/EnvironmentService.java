package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvironmentRepository;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.appEnvConfigurer.AppEnvConfigurerService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EnvironmentService {
  private final EnvironmentRepository repository;
  private final AppEnvConfigurerService configurerService;

  public List<Environment> findAllByApplicationId(String applicationId) {
    return repository.findAllByApplicationId(applicationId);
  }

  public Environment getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("Environment identified by id " + id + " not found"));
  }

  public List<Environment> crupdateEnvironments(
      String applicationId, List<Environment> environments) {
    return repository.saveAll(environments);
  }

  public final OneOfPojaConf configureEnvironment(
      String userId, String appId, String environmentId, OneOfPojaConf pojaConf) {
    return configurerService.configureEnvironment(userId, appId, environmentId, pojaConf);
  }

  public final OneOfPojaConf getConfig(String userId, String appId, String environmentId) {
    Environment linkedEnvironment = getById(environmentId);
    String configurationFileKey = linkedEnvironment.getConfigurationFileKey();
    return configurerService.readConfig(userId, appId, environmentId, configurationFileKey);
  }
}
