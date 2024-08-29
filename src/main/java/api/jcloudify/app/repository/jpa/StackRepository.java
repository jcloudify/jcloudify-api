package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.Stack;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StackRepository extends JpaRepository<Stack, String> {
  Optional<Stack> findByApplicationIdAndEnvironmentIdAndId(
      String applicationId, String environmentId, String id);

  List<Stack> findAllByEnvironmentId(String environmentId);
}
