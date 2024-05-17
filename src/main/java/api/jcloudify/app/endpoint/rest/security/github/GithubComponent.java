package api.jcloudify.app.endpoint.rest.security.github;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class GithubComponent {
    public String getEmailByToken(String token){
        GHMyself currentUser;
        try {
            GitHub gitHub = new GitHubBuilder().withOAuthToken(token).build();
            currentUser = gitHub.getMyself();
            log.info("The actual user: {}", currentUser);
            return currentUser.getEmail();
        } catch (IOException e) {
            return null;
        }
    }
}
