package api.jcloudify.app.endpoint.rest.security;

import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.endpoint.rest.security.model.Principal;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.service.UserService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class AuthProvider extends AbstractUserDetailsAuthenticationProvider {
  public static final String BEARER_PREFIX = "Bearer ";
  private final GithubComponent githubComponent;
  private final UserService userService;

  @Override
  protected void additionalAuthenticationChecks(
      UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
      throws AuthenticationException {
    // nothing
  }

  @Override
  protected UserDetails retrieveUser(
      String username, UsernamePasswordAuthenticationToken authentication)
      throws AuthenticationException {
    String bearer = getBearerFromHeader(authentication);
    if (bearer == null) {
      throw new UsernameNotFoundException("Bad credentials"); // NOSONAR
    }
    Optional<String> email = githubComponent.getEmailByToken(bearer);
    if (email.isEmpty()) {
      throw new UsernameNotFoundException("Bad credentials"); // NOSONAR
    }
    User user = userService.findByEmail(String.valueOf(email));
    return new Principal(user, bearer);
  }

  private String getBearerFromHeader(
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
    Object tokenObject = usernamePasswordAuthenticationToken.getCredentials();
    if (!(tokenObject instanceof String token) || !token.startsWith(BEARER_PREFIX)) {
      return null;
    }
    return token.substring(BEARER_PREFIX.length()).trim();
  }
}
