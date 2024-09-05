package api.jcloudify.app.endpoint.event.model;

import static java.util.UUID.randomUUID;

import api.jcloudify.app.service.pricing.PricingMethod;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@AllArgsConstructor
public class RefreshUserBillingInfoRequested extends PojaEvent {
  private final UUID id = randomUUID();
  private final String userId;
  private final RefreshUsersBillingInfoTriggered refreshUsersBillingInfoTriggered;
  private final PricingMethod pricingMethod;

  public final Instant getPricingCalculationRequestStartTime() {
    return refreshUsersBillingInfoTriggered.getUtcStartOfDay();
  }

  public final Instant getPricingCalculationRequestEndTime() {
    return refreshUsersBillingInfoTriggered.getNow();
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
