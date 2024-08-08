package api.jcloudify.app.endpoint.rest.security.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
  ADMIN,
  USER;

  public String getRole() {
    return name();
  }

  @Override
  public String getAuthority() {
    return "ROLE_" + getRole();
  }
}
