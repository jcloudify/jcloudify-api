package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.AppEnvironmentDeployment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentDeploymentRepository
    extends JpaRepository<AppEnvironmentDeployment, String> {}
