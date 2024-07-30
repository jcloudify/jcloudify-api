package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.SsmParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SsmParameterRepository extends JpaRepository<SsmParameter, String> {

  @Query(
      "SELECT p FROM SsmParameter p INNER JOIN Environment e ON e.id = p.environmentId "
          + "INNER JOIN Application a ON a.id = e.applicationId "
          + "WHERE a.id =?2 AND e.id = ?3 and a.userId = ?1")
  Page<SsmParameter> findByCriteria(String userId, String appId, String envId, Pageable pageable);
}
