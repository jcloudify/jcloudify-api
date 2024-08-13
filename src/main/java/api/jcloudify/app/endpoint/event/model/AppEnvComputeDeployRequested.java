package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.event.EventStack.EVENT_STACK_1;

import api.jcloudify.app.endpoint.event.EventStack;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
public class AppEnvComputeDeployRequested extends PojaEvent {
  @JsonProperty("formatted_bucket_key")
  private final String formattedBucketKey;

  @JsonProperty("app_name")
  private final String appName;

  @JsonProperty("environment_type")
  private final EnvironmentType environmentType;

  @JsonProperty("request_instant")
  private final Instant requestInstant;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(50);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }

  /**
   * this method is unused as our api will never consume this event due to using a different
   * event.source
   *
   * @return event stack used as sqs source
   */
  @Override
  public EventStack getEventStack() {
    return EVENT_STACK_1;
  }

  /**
   * specifies the event source, used for event consuming and producing. the event producing rule
   * will route this to the deployer app
   *
   * @return jcloudify.app.deployer event source
   */
  @Override
  public String getEventSource() {
    return "app.jcloudify.app.deployer.event";
  }
}
