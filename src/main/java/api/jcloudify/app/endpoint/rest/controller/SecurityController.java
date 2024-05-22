package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.service.GithubService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SecurityController {
  private final GithubService githubService;

  @GetMapping("/token")
  public Token exchangeCodeToToken(@RequestParam String code) {
    return githubService.exchangeCodeToToken(code);
  }
}
