package api.jcloudify.app.service;

import static api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus.FINISHED;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.BillingInfoRepository;
import api.jcloudify.app.repository.model.BillingInfo;
import api.jcloudify.app.repository.model.Environment;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class BillingInfoService {
  private final BillingInfoRepository repository;
  private final ApplicationService applicationService;
  private final EnvironmentService environmentService;

  public BillingInfo getUserBillingInfoByEnvironment(
      String userId, String appId, String envId, Instant startTime, Instant endTime) {
    return repository
        .findLatestByCriteria(userId, appId, envId, FINISHED, startTime, endTime)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "No billing info found for the environment "
                        + envId
                        + " within the specified time range"));
  }

  public List<BillingInfo> getUserBillingInfoByApplication(
      String userId, String appId, Instant startTime, Instant endTime) {
    List<BillingInfo> billingInfos = new ArrayList<>();
    List<Environment> applicationEnvironments =
        applicationService.getById(appId, userId).getEnvironments();
    applicationEnvironments.forEach(
        environment -> {
          try {
            billingInfos.add(
                getUserBillingInfoByEnvironment(
                    userId, appId, environment.getId(), startTime, endTime));
          } catch (NotFoundException e) {
            log.info(e.getMessage());
          }
        });
    return billingInfos;
  }
}
