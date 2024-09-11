package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.repository.model.Environment;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, String> {
  List<Environment> findAllByApplicationIdAndArchived(String applicationId, boolean isArchived);
  List<Environment> findAllByApplicationId(String applicationId);

  Optional<Environment> findFirstByApplicationIdAndEnvironmentTypeAndArchived(
      String applicationId, EnvironmentType environmentType, boolean isArchived);

  @Query(
      "SELECT e FROM Environment e INNER JOIN Application a ON a.id = e.applicationId WHERE e.id ="
          + " ?3 AND e.applicationId = ?2 AND a.userId = ?1")
  Optional<Environment> findByCriteria(String userId, String appId, String id);

  @Query(
      "SELECT e FROM Environment e INNER JOIN Application a ON a.id = e.applicationId WHERE"
          + " e.applicationId = ?2 AND a.userId = ?1 AND e.environmentType = ?3")
  Optional<Environment> findByCriteria(
      String userId, String appId, EnvironmentType environmentType);

  @Modifying
  @Query("update Environment e set e.configurationFileKey = ?2 where e.id = ?1")
  void updateEnvironmentConfigFileKey(String id, String fileKey);
}
