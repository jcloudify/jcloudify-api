package api.jcloudify.app.repository.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "\"env_deployment_conf\"")
@EqualsAndHashCode
@ToString
public class EnvDeploymentConf {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String computePermissionStackFileKey;
  private String eventStackFileKey;
  private String storageBucketStackFileKey;
  private String storageDatabaseSqliteStackFileKey;
  private Instant creationDatetime;
  private String buildTemplateFile;

  @Column(name = "env_id")
  private String envId;
}
