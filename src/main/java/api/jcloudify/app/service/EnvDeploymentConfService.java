package api.jcloudify.app.service;

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
}
