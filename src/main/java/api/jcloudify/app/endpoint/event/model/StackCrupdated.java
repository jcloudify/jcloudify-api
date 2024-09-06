package api.jcloudify.app.endpoint.event.model;

import api.jcloudify.app.repository.model.Stack;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder(toBuilder = true)
public class StackCrupdated extends PojaEvent {
  private String userId;
  private Stack stack;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(1);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(5);
  }
}
