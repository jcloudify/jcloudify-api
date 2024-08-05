package api.jcloudify.app.endpoint.rest.security;

import api.jcloudify.app.model.exception.ApiException;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Primary
@Component
@AllArgsConstructor
public class UsernamePasswordAuthenticatorFacade implements UsernamePasswordAuthenticator {
  private final GithubAppBearerAuthenticator githubAppBearerAuthenticator;
  private final GithubUserBearerAuthenticator githubUserBearerAuthenticator;

  @Override
  public UserDetails retrieveUser(
      String username, UsernamePasswordAuthenticationToken authentication) {
    try {
      return githubAppBearerAuthenticator.retrieveUser(username, authentication);
    } catch (AuthenticationException | ApiException ignored) {
      return githubUserBearerAuthenticator.retrieveUser(username, authentication);
    }
  }
}
