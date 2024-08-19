package api.jcloudify.app.service;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvDeploymentConfRepository;
import api.jcloudify.app.repository.model.EnvDeploymentConf;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvDeploymentConfService {
  private final EnvDeploymentConfRepository repository;

  public EnvDeploymentConf save(EnvDeploymentConf envDeploymentConf) {
    return repository.save(envDeploymentConf);
  }

  public EnvDeploymentConf getLatestByEnvId(String envId) {
    return repository
        .findTopByEnvIdOrderByCreationDatetimeDesc(envId)
        .orElseThrow(() -> new NotFoundException("No deployment conf found for env id=" + envId));
  }
}
