package api.jcloudify.app.service;

import static api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus.FINISHED;
import static java.math.BigDecimal.*;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.BillingInfoRepository;
import api.jcloudify.app.repository.model.BillingInfo;
import api.jcloudify.app.repository.model.Environment;
import java.math.BigDecimal;
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
    if (billingInfos.isEmpty()) {
      List<BillingInfo> emptyBillingInfos =
          applicationEnvironments.stream()
              .map(
                  env ->
                      BillingInfo.builder()
                          .userId(userId)
                          .appId(appId)
                          .envId(env.getId())
                          .computedPriceInUsd(ZERO)
                          .computedDurationInMinutes(0)
                          .computedMemoryUsedInMo(0)
                          .build())
              .toList();
      billingInfos.addAll(emptyBillingInfos);
    }
    return billingInfos;
  }

  public BillingInfo getUserBillingInfo(String userId, Instant startTime, Instant endTime) {
    List<BillingInfo> userBillingInfos =
        applicationService.findAllByUserId(userId).stream()
            .map(
                application ->
                    getUserBillingInfoByApplication(
                        userId, application.getId(), startTime, endTime))
            .flatMap(List::stream)
            .toList();
    int totalMemory =
        userBillingInfos.stream()
            .map(billingInfo -> billingInfo.getComputedMemoryUsedInMo())
            .reduce(0, (subtotal, memoryUsed) -> subtotal + memoryUsed);
    int totalDuration =
        userBillingInfos.stream()
            .map(billingInfo -> billingInfo.getComputedDurationInMinutes())
            .reduce(0, (subtotal, duration) -> subtotal + duration);
    BigDecimal totalPrice =
        userBillingInfos.stream()
            .map(billingInfo -> billingInfo.getComputedPriceInUsd())
            .reduce(ZERO, (subtotal, price) -> subtotal.add(price));
    String pricingMethod = userBillingInfos.getFirst().getPricingMethod();

    return BillingInfo.builder()
        .computedPriceInUsd(totalPrice)
        .computedMemoryUsedInMo(totalMemory)
        .computedDurationInMinutes(totalDuration)
        .pricingMethod(pricingMethod)
        .build();
  }
}
