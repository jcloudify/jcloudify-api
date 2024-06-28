package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.event.EventStack.EVENT_STACK_2;
import static java.lang.Math.random;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.endpoint.event.EventStack;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@PojaGenerated
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class DurablyFallibleUuidCreated2 extends PojaEvent {
  private UuidCreated uuidCreated;
  private int waitDurationBeforeConsumingInSeconds;
  private double failureRate;

  public boolean shouldFail() {
    return random() < failureRate;
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(
        waitDurationBeforeConsumingInSeconds + uuidCreated.maxConsumerDuration().toSeconds());
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return uuidCreated.maxConsumerBackoffBetweenRetries();
  }

  @Override
  public EventStack getEventStack() {
    return EVENT_STACK_2;
  }
}
