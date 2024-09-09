package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.event.EventStack.EVENT_STACK_2;

import api.jcloudify.app.endpoint.event.EventStack;
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
public class RefreshEnvBillingInfoRequested extends PojaEvent {
  private final String envId;
  private final String userId;
  private final String appId;
  private final RefreshAppBillingInfoRequested refreshAppBillingInfoRequested;

  public final PricingMethod getPricingMethod() {
    return refreshAppBillingInfoRequested.getPricingMethod();
  }

  public final Instant getPricingCalculationRequestStartTime() {
    return refreshAppBillingInfoRequested.getPricingCalculationRequestStartTime();
  }

  public final Instant getPricingCalculationRequestEndTime() {
    return refreshAppBillingInfoRequested.getPricingCalculationRequestEndTime();
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(10);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }

  @Override
  public EventStack getEventStack() {
    return EVENT_STACK_2;
  }
}
