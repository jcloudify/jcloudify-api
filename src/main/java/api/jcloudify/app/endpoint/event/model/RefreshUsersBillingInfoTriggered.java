package api.jcloudify.app.endpoint.event.model;

import static java.util.UUID.randomUUID;

import java.time.Duration;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@AllArgsConstructor
public class RefreshUsersBillingInfoTriggered extends PojaEvent {
  private final UUID id = randomUUID();

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
