package api.jcloudify.app.repository.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static java.util.Comparator.comparing;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import api.jcloudify.app.endpoint.rest.model.Environment.StateEnum;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "\"environment\"")
@EqualsAndHashCode
@ToString
public class Environment implements Serializable {
  @Id private String id;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  @Column(name = "environment_type")
  private EnvironmentType environmentType;

  private boolean archived;

  @Column(name = "id_application")
  private String applicationId;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  @Column(name = "state")
  private StateEnum state;

  private String configurationFileKey;
  private String codeFileKey;
  private Instant creationDatetime;

  /**
   * @param configurationFileKey non formatted s3 file key, will need to be formatted using
   *     ExtendedBucketComponent .getBucketKey to get the real filename
   */
  @Builder(toBuilder = true)
  public Environment(
      String id,
      EnvironmentType environmentType,
      boolean archived,
      String applicationId,
      StateEnum state,
      String configurationFileKey,
      String codeFileKey,
      List<EnvDeploymentConf> envDeploymentConfs,
      Instant creationDatetime) {
    this.id = id;
    this.environmentType = environmentType;
    this.archived = archived;
    this.applicationId = applicationId;
    this.state = state;
    this.configurationFileKey = configurationFileKey;
    this.codeFileKey = codeFileKey;
    this.envDeploymentConfs = envDeploymentConfs;
    this.creationDatetime = creationDatetime;
  }

  @OneToMany(cascade = ALL)
  @JoinColumn(name = "env_id", insertable = false, updatable = false)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private List<EnvDeploymentConf> envDeploymentConfs;

  public EnvDeploymentConf getLatestDeploymentConf() {
    return envDeploymentConfs.stream()
        .max(comparing(EnvDeploymentConf::getCreationDatetime))
        .orElseThrow();
  }

  @JsonIgnore
  public String getFormattedEnvironmentType() {
    return environmentType.toString().toLowerCase();
  }

  @JsonIgnore
  public String getGhBranchName() {
    return environmentType.toString().toLowerCase();
  }
}
