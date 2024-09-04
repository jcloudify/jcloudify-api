package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.BillingInfo;
import api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BillingInfoRepository extends JpaRepository<BillingInfo, String> {

  @Query(
      "SELECT b FROM BillingInfo b WHERE b.userId = ?1 AND b.appId = ?2 AND b.envId = ?3 AND"
          + " b.status = ?4 AND b.creationDatetime BETWEEN ?5 AND ?6 ORDER BY b.creationDatetime"
          + " DESC")
  Optional<BillingInfo> findLatestByCriteria(
      String userId,
      String appId,
      String envId,
      BillingInfoComputeStatus status,
      Instant startTime,
      Instant endTime);
}
