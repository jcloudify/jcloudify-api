package api.jcloudify.app.repository.model;

import static io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType.SQL_ARRAY_TYPE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import api.jcloudify.app.endpoint.rest.security.model.UserRole;
import api.jcloudify.app.service.pricing.PricingMethod;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
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
public class User {
  @Id
  @GeneratedValue(strategy = IDENTITY)
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

  @Column(name = "avatar")
  private String avatar;

  private String stripeId;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private PricingMethod pricingMethod;
}
