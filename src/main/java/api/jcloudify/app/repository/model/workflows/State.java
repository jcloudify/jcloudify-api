package api.jcloudify.app.repository.model.workflows;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import api.jcloudify.app.endpoint.rest.model.ExecutionType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class State<T extends Enum<T>> {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  protected String id;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  protected T progressionStatus;

  @CreationTimestamp private Instant timestamp;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  protected ExecutionType executionType;
}
