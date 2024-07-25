package api.jcloudify.app.endpoint.rest.security.github;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.service.github.model.CreateRepoRequestBody;
import api.jcloudify.app.service.github.model.CreateRepoResponse;
import api.jcloudify.app.service.github.model.UpdateRepoRequestBody;
import api.jcloudify.app.service.github.model.UpdateRepoResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class GithubComponent {
  private final GithubConf conf;
  private final RestTemplate restTemplate;
  private final UriComponents githubApiBaseUri;

  public GithubComponent(
      GithubConf conf,
      RestTemplate restTemplate,
      @Value("${github.api.baseuri}") String githubApiBaseUri) {
    this.conf = conf;
    this.restTemplate = restTemplate;
    this.githubApiBaseUri = UriComponentsBuilder.fromHttpUrl(githubApiBaseUri).build();
    ;
  }

  public Optional<String> getGithubUserId(String token) {
    GHMyself currentUser;
    try {
      GitHub gitHub = new GitHubBuilder().withOAuthToken(token).build();
      currentUser = gitHub.getMyself();
      return Optional.of(String.valueOf(currentUser.getId()));
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
    ParameterizedTypeReference<Map<String, Object>> typeReference =
        new ParameterizedTypeReference<>() {};
    var response = restTemplate.exchange(conf.getTokenUrl(), POST, entity, typeReference);
    var responseBody = response.getBody();

    if (responseBody != null && !responseBody.containsKey("error")) {
      String accessToken = (String) responseBody.get("access_token");
      String tokenType = (String) responseBody.get("token_type");
      return new Token().accessToken(accessToken).tokenType(tokenType);
    }
    assert responseBody != null;
    throw new BadRequestException((String) responseBody.get("error_description"));
  }

  public URI createRepoFor(Application application, String token) {
    log.info("creating repo for {}", application);
    HttpHeaders headers = getGithubHttpHeaders(token);
    var requestBody =
        new CreateRepoRequestBody(
            application.getGithubRepositoryName(),
            application.getDescription(),
            application.isGithubRepositoryPrivate());
    HttpEntity<CreateRepoRequestBody> entity = new HttpEntity<>(requestBody, headers);

    CreateRepoResponse response =
        restTemplate.exchange(getCreateRepoUri(), POST, entity, CreateRepoResponse.class).getBody();
    return response.htmlUrl();
  }

  public URI updateRepoFor(Application application, String token, String githubUsername) {
    log.info("updating repo for {}", application);

    HttpHeaders headers = getGithubHttpHeaders(token);
    UpdateRepoRequestBody requestBody =
        new UpdateRepoRequestBody(
            application.getGithubRepositoryName(),
            application.getDescription(),
            application.isGithubRepositoryPrivate(),
            application.isArchived());
    HttpEntity<UpdateRepoRequestBody> entity = new HttpEntity<>(requestBody, headers);

    UpdateRepoResponse response =
        restTemplate
            .exchange(
                getUpdateRepoUri(application, githubUsername).toUriString(),
                PATCH,
                entity,
                UpdateRepoResponse.class)
            .getBody();

    return response.htmlUrl();
  }

  private UriComponents getUpdateRepoUri(Application application, String githubUsername) {
    return UriComponentsBuilder.fromUri(githubApiBaseUri.toUri())
        .path("/repos/Mahefaa/poja_application")
        .buildAndExpand(githubUsername, application.getPreviousGithubRepositoryName());
  }

  private static HttpHeaders getGithubHttpHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(APPLICATION_JSON));
    headers.set("Authorization", "Bearer " + token);
    headers.set("X-GitHub-Api-Version", "2022-11-28");
    return headers;
  }

  private URI getCreateRepoUri() {
    return UriComponentsBuilder.fromUri(githubApiBaseUri.toUri())
        .path("/user/repos")
        .build()
        .toUri();
  }
}
