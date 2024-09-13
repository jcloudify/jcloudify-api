package api.jcloudify.app.endpoint.rest.security.model;

import static api.jcloudify.app.endpoint.rest.security.model.ApplicationRole.GITHUB_APPLICATION;

import api.jcloudify.app.repository.model.Application;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
@ToString
public class ApplicationPrincipal implements UserDetails {
  private final Application application;
  private final String bearer;

  @Override
  public Collection<ApplicationRole> getAuthorities() {
    return List.of(GITHUB_APPLICATION);
  }

  @Override
  public String getPassword() {
    return bearer;
  }

  @Override
  public String getUsername() {
    return bearer;
  }

  public String getInstallationId() {
    return application.getInstallationId();
  }

  @Override
  public boolean isAccountNonExpired() {
    return isEnabled();
  }

  @Override
  public boolean isAccountNonLocked() {
    return isEnabled();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isEnabled();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
