package api.jcloudify.app.model;

import api.jcloudify.app.endpoint.rest.security.model.UserRole;
import api.jcloudify.app.service.pricing.PricingMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class User {
  private String id;

  private String firstName;

  private String lastName;

  private String username;

  private String email;

  private UserRole[] roles;

  private String githubId;

  private String avatar;

  private String stripeId;

  private PricingMethod pricingMethod;

  private boolean betaTester;
}
