package api.jcloudify.app.endpoint.rest.security.github;

import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubComponent {
  public Optional<String> getEmailByToken(String token) {
    GHMyself currentUser;
    try {
      GitHub gitHub = new GitHubBuilder().withOAuthToken(token).build();
      currentUser = gitHub.getMyself();
      return Optional.of(currentUser.getEmail());
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
