package api.jcloudify.app.endpoint.rest.security;

import static api.jcloudify.app.endpoint.rest.security.Utils.getBearerFromHeader;

import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.endpoint.rest.security.model.ApplicationPrincipal;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.service.ApplicationService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class GithubAppBearerAuthenticator implements UsernamePasswordAuthenticator {
  private final GithubComponent githubComponent;
  private final ApplicationService applicationService;

  @Override
  public UserDetails retrieveUser(
      String username, UsernamePasswordAuthenticationToken authentication) {
    String bearer = getBearerFromHeader(authentication);
    if (bearer == null) {
      throw new UsernameNotFoundException("Bad credentials"); // NOSONAR
    }
    Optional<String> repositoryId = githubComponent.getRepositoryIdByAppToken(bearer);
    if (repositoryId.isEmpty()) {
      throw new UsernameNotFoundException("Bad credentials"); // NOSONAR
    }
    String repoId = repositoryId.get();
    try {
      Application application = applicationService.findByRepositoryId(repoId);
      return new ApplicationPrincipal(application, bearer);
    } catch (Exception e) {
      throw new UsernameNotFoundException(
          "Application with github repository id " + repoId + " not found");
    }
  }
}
