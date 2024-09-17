package api.jcloudify.app.service;

import static api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus.FINISHED;
import static java.math.BigDecimal.ZERO;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.BillingInfoRepository;
import api.jcloudify.app.repository.model.BillingInfo;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.User;
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
  private final UserService userService;

  public BillingInfo getUserBillingInfoByEnvironment(
      String userId, String appId, String envId, Instant startTime, Instant endTime) {
    User user = userService.getUserById(userId);
    return repository
        .findLatestByCriteria(userId, appId, envId, FINISHED, startTime, endTime)
        .orElse(
            BillingInfo.builder()
                .userId(userId)
                .appId(appId)
                .envId(envId)
                .pricingMethod(user.getPricingMethod())
                .computedPriceInUsd(ZERO)
                .computedDurationInMinutes(0)
                .build());
  }

  public List<BillingInfo> getUserBillingInfoByApplication(
      String userId, String appId, Instant startTime, Instant endTime) {
    List<BillingInfo> billingInfos = new ArrayList<>();
    List<Environment> applicationEnvironments =
        applicationService.getById(appId, userId).getEnvironments();
    applicationEnvironments.forEach(
        environment -> {
          billingInfos.add(
              getUserBillingInfoByEnvironment(
                  userId, appId, environment.getId(), startTime, endTime));
        });
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
    int totalDuration =
        userBillingInfos.stream()
            .map(billingInfo -> billingInfo.getComputedDurationInMinutes())
            .reduce(0, (subtotal, duration) -> subtotal + duration);
    BigDecimal totalPrice =
        userBillingInfos.stream()
            .map(billingInfo -> billingInfo.getComputedPriceInUsd())
            .reduce(ZERO, (subtotal, price) -> subtotal.add(price));

    return BillingInfo.builder()
        .computedPriceInUsd(totalPrice)
        .computedDurationInMinutes(totalDuration)
        .pricingMethod(userBillingInfos.getFirst().getPricingMethod())
        .build();
  }

  public BillingInfo crupdateBillingInfo(BillingInfo toSave) {
    return repository.save(toSave);
  }

  public BillingInfo getByQueryId(String queryId) {
    return repository
        .findByQueryId(queryId)
        .orElseThrow(() -> new NotFoundException("Not billing info found for query id " + queryId));
  }
}
