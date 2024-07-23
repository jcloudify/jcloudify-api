package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.repository.model.Stack;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StackRepository extends JpaRepository<Stack, String> {
  Optional<Stack> findByApplicationIdAndEnvironmentIdAndTypeAndId(
      String applicationId, String environmentId, StackType type, String id);

  Optional<Stack> findByApplicationIdAndEnvironmentIdAndId(
      String applicationId, String environmentId, String id);
}
