package api.jcloudify.app.endpoint.rest.security.github;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GithubConf {
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String tokenUrl;

  public GithubConf(
      @Value("${github.client.id}") String clientId,
      @Value("${github.client.secret}") String clientSecret,
      @Value("${github.redirect.uri}") String redirectUri,
      @Value("${github.token.url}") String tokenUrl) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.tokenUrl = tokenUrl;
  }
}
