package api.jcloudify.app.service;

import static api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus.FINISHED;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.BillingInfoRepository;
import api.jcloudify.app.repository.model.BillingInfo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BillingInfoService {
  private final BillingInfoRepository repository;

  public BillingInfo getUserBillingInfoByEnvironment(
      String userId, String appId, String envId, Instant startTime, Instant endTime) {
    return repository
        .findLatestByCriteria(userId, appId, envId, FINISHED, startTime, endTime)
        .orElseThrow(() -> new NotFoundException("No billing info found"));
  }
}
