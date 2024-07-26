package api.jcloudify.app.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "\"app_installation\"")
@EqualsAndHashCode
@ToString
public class AppInstallation {
  @Id private String id;
  private long ghId;
  private String userId;
  private String ownerGithubLogin;
  private boolean isOrg;
}
