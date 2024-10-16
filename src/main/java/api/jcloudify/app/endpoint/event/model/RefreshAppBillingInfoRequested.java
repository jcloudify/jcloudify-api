package api.jcloudify.app.endpoint.event.model;

import api.jcloudify.app.service.pricing.PricingMethod;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@AllArgsConstructor
public class RefreshAppBillingInfoRequested extends PojaEvent {
  private final String userId;
  private final String appId;
  private final RefreshUserBillingInfoRequested refreshUserBillingInfoRequested;

  public final PricingMethod getPricingMethod() {
    return refreshUserBillingInfoRequested.getPricingMethod();
  }

  public final Instant getPricingCalculationRequestStartTime() {
    return refreshUserBillingInfoRequested.getPricingCalculationRequestStartTime();
  }

  public final Instant getPricingCalculationRequestEndTime() {
    return refreshUserBillingInfoRequested.getPricingCalculationRequestEndTime();
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
