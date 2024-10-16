package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.event.model.ApplicationCrupdated.CrupdateType.CREATE;
import static api.jcloudify.app.endpoint.event.model.ApplicationCrupdated.CrupdateType.UPDATE;

import java.time.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ApplicationCrupdated extends PojaEvent {
  private final String applicationId;
  private final String applicationRepoName;
  private final String previousApplicationRepoName;
  private final String description;
  private final boolean repoPrivate;
  private final String installationId;
  private final boolean archived;
  private final CrupdateType crupdateType;
  private final String repoUrl;

  @Builder(toBuilder = true)
  public ApplicationCrupdated(
      String applicationId,
      String applicationRepoName,
      String previousApplicationRepoName,
      String description,
      boolean repoPrivate,
      String installationId,
      boolean archived,
      String repoUrl) {
    this.applicationId = applicationId;
    this.applicationRepoName = applicationRepoName;
    this.previousApplicationRepoName = previousApplicationRepoName;
    this.description = description;
    this.repoPrivate = repoPrivate;
    this.installationId = installationId;
    this.archived = archived;
    this.repoUrl = repoUrl;
    this.crupdateType = repoUrl == null ? CREATE : UPDATE;
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(50);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }

  public enum CrupdateType {
    CREATE,
    UPDATE
  }
}
