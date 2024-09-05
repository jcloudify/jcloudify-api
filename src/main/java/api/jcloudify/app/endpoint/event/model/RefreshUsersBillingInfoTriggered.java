package api.jcloudify.app.endpoint.event.model;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
  private final LocalDate utcLocalDate = LocalDate.now(UTC);
  private final Instant utcStartOfDay = utcLocalDate.atStartOfDay(UTC).toInstant();
  private final Instant now = Instant.now();

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
