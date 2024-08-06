package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.EnvDeploymentConf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvDeploymentConfRepository extends JpaRepository<EnvDeploymentConf, String> {}
