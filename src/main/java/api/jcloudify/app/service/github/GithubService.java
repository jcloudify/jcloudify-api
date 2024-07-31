package api.jcloudify.app.service.github;

import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.service.github.model.CreateRepoRequestBody;
import api.jcloudify.app.service.github.model.GhAppInstallation;
import api.jcloudify.app.service.github.model.UpdateRepoRequestBody;
import java.net.URI;
import java.time.Duration;
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

  public URI createRepoFor(CreateRepoRequestBody requestBody, String token) {
    return githubComponent.createRepoFor(requestBody, token);
  }

  public URI updateRepoFor(
      UpdateRepoRequestBody application,
      String repositoryName,
      String token,
      String githubUsername) {
    return githubComponent.updateRepoFor(application, repositoryName, token, githubUsername);
  }

  public Set<GhAppInstallation> listApplications() {
    return githubComponent.listInstallations();
  }

  public String getInstallationToken(long installationId, Duration duration) {
    return githubComponent.getAppInstallationToken(installationId, duration);
  }

  public GhAppInstallation getInstallationByGhId(long ghId) {
    return githubComponent.getInstallationById(ghId);
  }
}
