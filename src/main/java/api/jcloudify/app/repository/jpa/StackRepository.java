package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.repository.model.Stack;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StackRepository extends JpaRepository<Stack, String> {
  Optional<Stack> findByApplicationIdAndEnvironmentIdAndType(
      String applicationId, String environmentId, StackType type);

  List<Stack> findAllByApplicationIdAndEnvironmentId(String applicationId, String environmentId);
}
