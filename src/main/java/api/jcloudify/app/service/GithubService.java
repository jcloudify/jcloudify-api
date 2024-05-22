package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class GithubService {
  private final GithubComponent githubComponent;

  public Token exchangeCodeToToken(String code) {
    return githubComponent.exchangeCodeToToken(code);
  }
}
