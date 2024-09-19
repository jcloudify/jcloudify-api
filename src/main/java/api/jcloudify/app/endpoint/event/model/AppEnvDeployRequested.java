package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo.JSON_PROPERTY_FORMATTED_BUCKET_KEY;

import api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Builder(toBuilder = true)
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
  @JsonIgnoreProperties(JSON_PROPERTY_FORMATTED_BUCKET_KEY)
  private final BuiltEnvInfo builtEnvInfo;

  /**
   * built_zip_formatted_key overrides built_env_info formatted bucket key as builtEnvInfo's value
   * tend to be the temporary file storage
   */
  @JsonProperty("built_zip_formatted_key")
  private final String builtZipFormattedFilekey;

  @JsonProperty("deployment_conf_id")
  private final String deploymentConfId;

  // Used to check if able to correctly deploy compute stack or not
  @JsonProperty("independent_stack_states")
  private final IndependentStacksStateEnum independentStacksStates;

  @JsonProperty("app_env_deployment_id")
  private String appEnvDeploymentId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
