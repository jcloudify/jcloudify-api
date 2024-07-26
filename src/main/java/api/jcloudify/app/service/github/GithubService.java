package api.jcloudify.app.service.github;

import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.service.github.model.GhAppInstallation;
import java.net.URI;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GithubService {
  private final GithubComponent githubComponent;

  public Token exchangeCodeToToken(String code) {
    return githubComponent.exchangeCodeToToken(code);
  }

  public URI createRepoFor(Application application, String token) {
    return githubComponent.createRepoFor(application, token);
  }

  public URI updateRepoFor(Application application, String token, String githubUsername) {
    return githubComponent.updateRepoFor(application, token, githubUsername);
  }

  public Set<GhAppInstallation> listApplications() {
    return githubComponent.listApplications();
  }
}
