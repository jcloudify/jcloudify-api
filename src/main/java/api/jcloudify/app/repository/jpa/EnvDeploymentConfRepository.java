package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.EnvDeploymentConf;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvDeploymentConfRepository extends JpaRepository<EnvDeploymentConf, String> {
  Optional<EnvDeploymentConf> findTopByEnvIdOrderByCreationDatetimeDesc(String envId);
}
