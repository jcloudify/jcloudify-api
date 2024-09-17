package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.DeploymentProgression;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentProgressionRepository
    extends JpaRepository<DeploymentProgression, String> {
  List<DeploymentProgression> findAllByAppEnvDeploymentId(String deploymentId, Pageable pageable);
}
