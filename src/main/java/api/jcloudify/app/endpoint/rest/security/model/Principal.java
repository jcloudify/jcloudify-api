package api.jcloudify.app.endpoint.rest.security.model;

import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.repository.model.enums.UserRole;
import java.util.Arrays;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
@ToString
public class Principal implements UserDetails {
  private final User user;

  private final String bearer;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Arrays.stream(user.getRoles())
        .map(role -> UserRole.valueOf(String.valueOf(role)))
        .toList();
  }

  @Override
  public String getPassword() {
    return bearer;
  }

  @Override
  public String getUsername() {
    return user.getUsername();
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
