package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.repository.model.Environment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, String> {
  List<Environment> findAllByApplicationId(String applicationId);

  Optional<Environment> findFirstByApplicationIdAndEnvironmentType(
      String applicationId, EnvironmentType environmentType);

  @Query(
      "SELECT e FROM Environment e INNER JOIN Application a ON a.id = e.applicationId WHERE e.id ="
          + " ?3 AND e.applicationId = ?2 AND a.userId = ?1")
  Optional<Environment> findByCriteria(String userId, String appId, String id);
}
