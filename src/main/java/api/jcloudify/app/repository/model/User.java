package api.jcloudify.app.repository.model;

import static io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType.SQL_ARRAY_TYPE;

import api.jcloudify.app.repository.model.enums.UserRole;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"user\"")
@EqualsAndHashCode
@ToString
public class User implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "username")
  private String username;

  @Column(name = "email")
  private String email;

  @Type(
      value = EnumArrayType.class,
      parameters = @Parameter(name = SQL_ARRAY_TYPE, value = "user_role"))
  @Column(name = "roles", columnDefinition = "user_role[]")
  private UserRole[] roles;

  @Column(name = "github_id")
  private String githubId;

  @ManyToOne
  @JoinColumn(name = "id_plan")
  private Plan plan;
}
