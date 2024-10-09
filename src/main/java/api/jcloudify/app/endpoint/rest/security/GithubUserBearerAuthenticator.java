package api.jcloudify.app.endpoint.rest.security;

import static api.jcloudify.app.endpoint.rest.security.Utils.getBearerFromHeader;

import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.endpoint.rest.security.model.Principal;
import api.jcloudify.app.model.User;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.service.UserService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class GithubUserBearerAuthenticator implements UsernamePasswordAuthenticator {
  private final GithubComponent githubComponent;
  private final UserService userService;

  @Override
  public UserDetails retrieveUser(
      String username, UsernamePasswordAuthenticationToken authentication) {
    String bearer = getBearerFromHeader(authentication);
    if (bearer == null) {
      throw new UsernameNotFoundException("Bad credentials"); // NOSONAR
    }
    Optional<String> githubUserId = githubComponent.getGithubUserId(bearer);
    if (githubUserId.isEmpty()) {
      throw new UsernameNotFoundException("Bad credentials"); // NOSONAR
    }
    try {
      User user = userService.findByGithubUserId(githubUserId.get());
      return new Principal(user, bearer);
    } catch (NotFoundException e) {
      throw new UsernameNotFoundException(
          "User with github user id " + githubUserId.get() + " not found");
    }
  }
}
