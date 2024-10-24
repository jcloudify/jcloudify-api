package api.jcloudify.app.endpoint.rest.security.github;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.service.github.model.CreateRepoRequestBody;
import api.jcloudify.app.service.github.model.CreateRepoResponse;
import api.jcloudify.app.service.github.model.GhAppInstallation;
import api.jcloudify.app.service.github.model.GhAppInstallationRepositoriesResponse;
import api.jcloudify.app.service.github.model.GhAppInstallationResponse;
import api.jcloudify.app.service.github.model.GhGetCommitResponse;
import api.jcloudify.app.service.github.model.UpdateRepoRequestBody;
import api.jcloudify.app.service.github.model.UpdateRepoResponse;
import api.jcloudify.app.service.jwt.JwtGenerator;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
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
  private final JwtGenerator jwtGenerator;
  private final int githubAppId;

  public GithubComponent(
      GithubConf conf,
      RestTemplate restTemplate,
      @Value("${github.api.baseuri}") String githubApiBaseUri,
      JwtGenerator jwtGenerator,
      @Value("${github.appid}") int githubAppId) {
    this.conf = conf;
    this.restTemplate = restTemplate;
    this.githubApiBaseUri = UriComponentsBuilder.fromHttpUrl(githubApiBaseUri).build();
    this.jwtGenerator = jwtGenerator;
    this.githubAppId = githubAppId;
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
    HttpHeaders headers = getHttpHeaders();

    Map<String, String> body = getBody();
    body.put("code", code);

    var responseBody = sendTokenRequest(body, headers);
    if (responseBody != null && !responseBody.containsKey("error")) {
      return extractToken(responseBody);
    }
    assert responseBody != null;
    throw new BadRequestException((String) responseBody.get("error_description"));
  }

  private Map<String, String> getBody() {
    Map<String, String> body = new HashMap<>();
    body.put("client_id", conf.getClientId());
    body.put("client_secret", conf.getClientSecret());
    body.put("redirect_uri", conf.getRedirectUri());
    return body;
  }

  private static HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setAccept(List.of(APPLICATION_JSON));
    return headers;
  }

  public Token refreshToken(String tokenToRefresh) {
    HttpHeaders headers = getHttpHeaders();

    Map<String, String> body = getBody();
    body.put("grant_type", "refresh_token");
    body.put("refresh_token", tokenToRefresh);

    var responseBody = sendTokenRequest(body, headers);

    if (responseBody != null && !responseBody.containsKey("error")) {
      return extractToken(responseBody);
    }
    assert responseBody != null;
    throw new BadRequestException((String) responseBody.get("error_description"));
  }

  private static Token extractToken(Map<String, Object> responseBody) {
    String accessToken = (String) responseBody.get("access_token");
    String tokenType = (String) responseBody.get("token_type");
    String refreshToken = (String) responseBody.get("refresh_token");
    return new Token().accessToken(accessToken).refreshToken(refreshToken).tokenType(tokenType);
  }

  private Map<String, Object> sendTokenRequest(Map<String, String> body, HttpHeaders headers) {
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
    ParameterizedTypeReference<Map<String, Object>> typeReference =
        new ParameterizedTypeReference<>() {};
    var response = restTemplate.exchange(conf.getTokenUrl(), POST, entity, typeReference);
    var responseBody = response.getBody();
    return responseBody;
  }

  public Optional<String> getRepositoryIdByAppToken(String token) {
    HttpHeaders headers = getGithubHttpHeaders(token);
    HttpEntity<GhAppInstallationRepositoriesResponse> entity = new HttpEntity<>(headers);

    var response =
        restTemplate.exchange(
            getAppInstallationRepositoriesUri().toUriString(),
            GET,
            entity,
            GhAppInstallationRepositoriesResponse.class);
    var responseBody = response.getBody();

    if (responseBody == null) {
      return Optional.empty();
    }
    log.info("Installation repositories: {}", responseBody);
    return Optional.of(String.valueOf(responseBody.repositories().getFirst().id()));
  }

  public CreateRepoResponse createRepoFor(CreateRepoRequestBody requestBody, String token) {
    log.info("creating repo for {}", requestBody);
    HttpHeaders headers = getGithubHttpHeaders(token);
    HttpEntity<CreateRepoRequestBody> entity = new HttpEntity<>(requestBody, headers);

    return restTemplate
        .exchange(getCreateRepoUri(), POST, entity, CreateRepoResponse.class)
        .getBody();
  }

  public UpdateRepoResponse updateRepoFor(
      UpdateRepoRequestBody requestBody,
      String repositoryName,
      String token,
      String repoOwnerUsername) {
    log.info("updating repo for {}", requestBody);
    HttpHeaders headers = getGithubHttpHeaders(token);
    HttpEntity<UpdateRepoRequestBody> entity = new HttpEntity<>(requestBody, headers);

    return restTemplate
        .exchange(
            getUpdateRepoUri(repositoryName, repoOwnerUsername).toUriString(),
            PATCH,
            entity,
            UpdateRepoResponse.class)
        .getBody();
  }

  private UriComponents getUpdateRepoUri(String repositoryName, String githubUsername) {
    return UriComponentsBuilder.fromUri(githubApiBaseUri.toUri())
        .path("/repos/{owner}/{repositoryName}")
        .buildAndExpand(githubUsername, repositoryName);
  }

  private UriComponents getListAppInstallationUri() {
    return UriComponentsBuilder.fromUri(githubApiBaseUri.toUri())
        .path("/app/installations")
        .build();
  }

  private UriComponents getAppInstallationByIdUri(long installationId) {
    return UriComponentsBuilder.fromUri(githubApiBaseUri.toUri())
        .path("/app/installations/{id}")
        .buildAndExpand(installationId);
  }

  private UriComponents getAppInstallationRepositoriesUri() {
    return UriComponentsBuilder.fromUri(githubApiBaseUri.toUri())
        .path("/installation/repositories")
        .build();
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
        .path("/repos/jcloudify/jcloudify-starter-template/generate")
        .build()
        .toUri();
  }

  public String getAppInstallationToken(long installationId, Duration expiration) {
    var jwtToken = jwtGenerator.createJwt(githubAppId, expiration);
    try {
      var gitHubApp = new GitHubBuilder().withJwtToken(jwtToken).build();
      return gitHubApp
          .getApp()
          .getInstallationById(installationId)
          .createToken()
          .create()
          .getToken();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Set<GhAppInstallation> listInstallations() {
    var jwtToken = jwtGenerator.createJwt(githubAppId, Duration.ofSeconds(60));
    HttpHeaders headers = getGithubHttpHeaders(jwtToken);
    HttpEntity<GhAppInstallationResponse> entity = new HttpEntity<>(headers);

    ParameterizedTypeReference<List<GhAppInstallationResponse>> typeRef =
        new ParameterizedTypeReference<>() {};
    var response =
        restTemplate
            .exchange(getListAppInstallationUri().toUriString(), GET, entity, typeRef)
            .getBody();

    return response.stream().map(GithubComponent::toDomain).collect(Collectors.toSet());
  }

  public GhAppInstallation getInstallationById(long id) {
    var jwtToken = jwtGenerator.createJwt(githubAppId, Duration.ofSeconds(30));
    HttpHeaders headers = getGithubHttpHeaders(jwtToken);
    HttpEntity<GhAppInstallationResponse> entity = new HttpEntity<>(headers);

    var response =
        restTemplate
            .exchange(
                getAppInstallationByIdUri(id).toUriString(),
                GET,
                entity,
                GhAppInstallationResponse.class)
            .getBody();
    return toDomain(response);
  }

  public GhGetCommitResponse getCommitInfo(
      String owner, long installationId, String repoName, String sha) {
    var jwtToken = getAppInstallationToken(installationId, Duration.ofMinutes(1));
    HttpHeaders headers = getGithubHttpHeaders(jwtToken);
    HttpEntity<GhGetCommitResponse> entity = new HttpEntity<>(headers);

    ParameterizedTypeReference<GhGetCommitResponse> typeRef = new ParameterizedTypeReference<>() {};
    return restTemplate
        .exchange(getGetCommitInfoUri(owner, repoName, sha).toUriString(), GET, entity, typeRef)
        .getBody();
  }

  private UriComponents getGetCommitInfoUri(String owner, String repoName, String sha) {
    return UriComponentsBuilder.fromUri(githubApiBaseUri.toUri())
        .path("/repos/{owner}/{repo}/commits/{ref}")
        .buildAndExpand(owner, repoName, sha);
  }

  @SneakyThrows
  private static GhAppInstallation toDomain(GhAppInstallationResponse installation) {
    var account = installation.account();
    var ownerLogin = account.login();
    String type = account.type();
    String avatarUrl = account.avatarUrl();
    return new GhAppInstallation(installation.id(), ownerLogin, type, avatarUrl);
  }
}
