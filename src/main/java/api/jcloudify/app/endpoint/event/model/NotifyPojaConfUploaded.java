package api.jcloudify.app.endpoint.event.model;

import java.time.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class NotifyPojaConfUploaded extends PojaEvent {
  public enum Status {
    SUCCESS,
    FAILURE;
  }

  private final Status status;
  private final String appId;
  private final String envId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(45);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
