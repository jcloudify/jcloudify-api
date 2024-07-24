package api.jcloudify.app.endpoint.event.model;

import api.jcloudify.app.model.PojaVersion;
import java.time.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public final class PojaConfUploaded extends PojaEvent {
  /**
   * constructor of PojaConfUploaded
   *
   * @param pojaVersion: cli_version
   * @param environmentId: environment configured with the conf
   * @param userId: poja conf owner userid
   * @param filename: refers to the s3 key without the prefixes (userId, environmentId, ...)
   * @param appId: configuredAppId
   */
  public PojaConfUploaded(
      PojaVersion pojaVersion, String environmentId, String userId, String filename, String appId) {
    this.pojaVersion = pojaVersion;
    this.environmentId = environmentId;
    this.userId = userId;
    this.filename = filename;
    this.appId = appId;
  }

  private final PojaVersion pojaVersion;
  private final String environmentId;
  private final String userId;
  private final String filename;
  private final String appId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
