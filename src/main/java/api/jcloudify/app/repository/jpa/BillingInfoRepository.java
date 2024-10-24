package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.BillingInfo;
import api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BillingInfoRepository extends JpaRepository<BillingInfo, String> {

  @Query(
      "SELECT b FROM BillingInfo b WHERE b.userId = ?1 AND b.appId = ?2 AND b.envId = ?3 AND"
          + " b.status = ?4 AND b.creationDatetime BETWEEN ?5 AND ?6 ORDER BY b.creationDatetime"
          + " DESC LIMIT 1")
  Optional<BillingInfo> findLatestByCriteria(
      String userId,
      String appId,
      String envId,
      BillingInfoComputeStatus status,
      Instant startTime,
      Instant endTime);

  Optional<BillingInfo> findByQueryId(String queryId);

  @Modifying
  @Query(
      "UPDATE BillingInfo b SET b.status = ?1, b.computeDatetime = ?2, b.computedDurationInMinutes"
          + " = ?3, b.computedPriceInUsd = ?4 WHERE b.id = ?5")
  void updateBillingInfoAttributes(
      BillingInfoComputeStatus status,
      Instant computeDatetime,
      double computedDurationInMinutes,
      BigDecimal computedPriceInUsd,
      String id);
}
