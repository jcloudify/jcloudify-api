package api.jcloudify.app.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"application\"")
@EqualsAndHashCode
@ToString
public class Application implements Serializable {
  @Id private String id;

  private String name;
  private boolean archived;

  private String githubRepositoryName;
  private boolean isGithubRepositoryPrivate;
  private String installationId;

  public long getPrice() {
    return 100L;
  }

  @Transient private String previousGithubRepositoryName;

  @CreationTimestamp private Instant creationDatetime;

  @Column(name = "id_user")
  private String userId;

  private String description;

  @OneToMany(mappedBy = "applicationId", cascade = CascadeType.ALL)
  private List<Environment> environments;

  @Column(insertable = false, name = "repo_http_url")
  private String githubRepositoryUrl;

  @Column(insertable = false)
  private String githubRepositoryId;

  @JsonIgnore
  public String getFormattedName() {
    return name.replace("_", "-");
  }
}
