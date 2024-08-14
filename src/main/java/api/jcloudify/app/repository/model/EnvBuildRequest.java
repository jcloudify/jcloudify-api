package api.jcloudify.app.repository.model;

import jakarta.persistence.Entity;
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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"env_build_request\"")
@EqualsAndHashCode
@ToString
public class EnvBuildRequest {
  @Id private String id;
  private String appId;
  private String envId;
  private String userId;
  @CreationTimestamp private Instant creationTimestamp;
  private String built_zip_file_key;
}
