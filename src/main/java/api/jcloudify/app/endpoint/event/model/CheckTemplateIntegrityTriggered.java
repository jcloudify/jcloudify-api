package api.jcloudify.app.endpoint.event.model;

import api.jcloudify.app.endpoint.event.EventStack;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
public class CheckTemplateIntegrityTriggered extends PojaEvent {
  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("app_id")
  private String appId;

  @JsonProperty("env_id")
  private String envId;

  @JsonProperty("built_project_bucket_key")
  private String builtProjectBucketKey;

  @JsonProperty("template_file_bucket_key")
  private String templateFileBucketKey;

  @JsonProperty("built_env_info")
  private BuiltEnvInfo builtEnvInfo;

  @JsonProperty("deployment_conf_id")
  private String deploymentConfId;

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
    throw new UnsupportedOperationException();
  }

  /**
   * specifies the event source, used for event consuming and producing. the event producing rule
   * will route this to the deployer app
   *
   * @return jcloudify.app.deployer event source
   */
  @Override
  public String getEventSource() {
    return "app.jcloudify.app.deployer.event.check";
  }
}
