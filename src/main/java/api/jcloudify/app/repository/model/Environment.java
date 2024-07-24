package api.jcloudify.app.repository.model;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import api.jcloudify.app.endpoint.rest.model.Environment.StateEnum;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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

  @JsonIgnore
  public String getFormattedEnvironmentType() {
    return environmentType.toString().toLowerCase();
  }
}
