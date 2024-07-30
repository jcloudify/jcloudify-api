package api.jcloudify.app.repository.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"ssm_parameter\"")
@EqualsAndHashCode
@ToString
public class SsmParameter implements Serializable {
  @Id private String id;

  @GeneratedValue(strategy = IDENTITY)
  private String name;

  @Column(name = "id_environment")
  private String environmentId;
}
