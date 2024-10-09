package api.jcloudify.app.endpoint.rest.security;

import api.jcloudify.app.endpoint.rest.security.model.ApplicationPrincipal;
import api.jcloudify.app.endpoint.rest.security.model.Principal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class AuthProvider extends AbstractUserDetailsAuthenticationProvider {
  private final UsernamePasswordAuthenticator authenticator;

  public static Principal getPrincipal() {
    return (Principal) getAuthentication().getPrincipal();
  }

  public static Authentication getAuthentication() {
    SecurityContext context = SecurityContextHolder.getContext();
    return context.getAuthentication();
  }

  public static ApplicationPrincipal getApplicationPrincipal() {
    return (ApplicationPrincipal) getAuthentication().getPrincipal();
  }

  @Override
  protected void additionalAuthenticationChecks(
      UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
      throws AuthenticationException {
    // nothing
  }

  @Override
  protected UserDetails retrieveUser(
      String username, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
      throws AuthenticationException {
    log.info("retrieving user");
    return authenticator.retrieveUser(username, usernamePasswordAuthenticationToken);
  }
}
