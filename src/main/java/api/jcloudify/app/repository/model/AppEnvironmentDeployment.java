package api.jcloudify.app.repository.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.URI;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"app_environment_deployment\"")
@EqualsAndHashCode
@ToString
public class AppEnvironmentDeployment {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String appId;
  private String envId;
  private String envDeplConfId;
  private URI deployedUrl;
  private Instant creationDatetime;
  private String ghCommitBranch;
  private String ghCommitAuthorName;
  private String ghCommitMessage;
  private String ghCommitSha;
  private String ghOrg;
  private String ghIsPushed;
  private String ghRepoId;
  private boolean ghIsRepoPrivate;
  private URI ghRepoUrl;
  private String ghRepoName;
  private String ghRepoOwnerType;
  private String creatorEmail;
  private String creatorUsername;
  private String creatorGhId;
  private URI creatorAvatarUrl;
}
