package api.jcloudify.app.endpoint.event.model;

import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
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
public class AppEnvDeployRequested extends PojaEvent {
  @JsonProperty("env_id")
  private final String envId;

  @JsonProperty("user_id")
  private final String userId;

  @JsonProperty("app_id")
  private final String appId;

  @JsonProperty("request_instant")
  private final Instant requestInstant;

  @JsonProperty("built_env_info")
  private final BuiltEnvInfo builtEnvInfo;

  @JsonProperty("deployment_conf_id")
  private final String deploymentConfId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
