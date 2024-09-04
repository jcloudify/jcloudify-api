package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.event.EventStack.EVENT_STACK_2;

import api.jcloudify.app.endpoint.event.EventStack;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@AllArgsConstructor
public class RefreshUsersLogStreamsTriggered extends PojaEvent {
  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(1);
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
