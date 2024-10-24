package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.DeploymentState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentStateRepository extends JpaRepository<DeploymentState, String> {
  List<DeploymentState> findAllByAppEnvDeploymentId(String deploymentId, Pageable pageable);
  Optional<DeploymentState> findByAppEnvDeploymentId(String deploymentId);
}
