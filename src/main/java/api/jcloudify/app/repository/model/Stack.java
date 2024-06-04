package api.jcloudify.app.repository.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import api.jcloudify.app.endpoint.rest.model.StackType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "\"stack\"")
@EqualsAndHashCode
@ToString
public class Stack implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String name;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private StackType type;

  @Column(name = "cf_stack_id")
  private String cfStackId;

  @Column(name = "id_environment")
  private String environmentId;

  @Column(name = "id_application")
  private String applicationId;
}