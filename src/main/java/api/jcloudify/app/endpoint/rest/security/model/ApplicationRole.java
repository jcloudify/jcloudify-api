package api.jcloudify.app.endpoint.rest.security.model;

import org.springframework.security.core.GrantedAuthority;

public enum ApplicationRole implements GrantedAuthority {
  GITHUB_APPLICATION;

  public String getRole() {
    return name();
  }

  @Override
  public String getAuthority() {
    return "ROLE_" + getRole();
  }
}
