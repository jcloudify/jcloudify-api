package api.jcloudify.app.endpoint.rest.security.github;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.model.exception.BadRequestException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHEmail;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@AllArgsConstructor
public class GithubComponent {
  private final GithubConf conf;
  private final RestTemplate restTemplate;

  public Optional<String> getEmailByToken(String token) {
    GHMyself currentUser;
    try {
      GitHub gitHub = new GitHubBuilder().withOAuthToken(token).build();
      currentUser = gitHub.getMyself();
      return Optional.of(extractPrimaryEmail(currentUser.getEmails2()));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public Optional<GHMyself> getCurrentUserByToken(String token) {
    try {
      GitHub gitHub = new GitHubBuilder().withOAuthToken(token).build();
      return Optional.of(gitHub.getMyself());
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private String extractPrimaryEmail(List<GHEmail> ghEmails) {
    return ghEmails.stream().filter(GHEmail::isPrimary).toList().getFirst().getEmail();
  }

  public Token exchangeCodeToToken(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setAccept(List.of(APPLICATION_JSON));

    Map<String, String> body = new HashMap<>();
    body.put("client_id", conf.getClientId());
    body.put("client_secret", conf.getClientSecret());
    body.put("code", code);
    body.put("redirect_uri", conf.getRedirectUri());

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response =
        restTemplate.exchange(conf.getTokenUrl(), POST, entity, Map.class);
    Map responseBody = response.getBody();

    if (responseBody != null && !responseBody.containsKey("error")) {
      String accessToken = (String) responseBody.get("access_token");
      String tokenType = (String) responseBody.get("token_type");
      return new Token().accessToken(accessToken).tokenType(tokenType);
    }
    assert responseBody != null;
    throw new BadRequestException((String) responseBody.get("error_description"));
  }
}
