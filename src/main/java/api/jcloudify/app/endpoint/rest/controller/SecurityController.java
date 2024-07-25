package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.UserMapper;
import api.jcloudify.app.endpoint.rest.model.Token;
import api.jcloudify.app.endpoint.rest.model.Whoami;
import api.jcloudify.app.endpoint.rest.security.model.Principal;
import api.jcloudify.app.service.github.GithubService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SecurityController {
  private final GithubService githubService;
  private final UserMapper userMapper;

  @GetMapping("/whoami")
  public Whoami whoami(@AuthenticationPrincipal Principal principal) {
    return new Whoami().user(userMapper.toRest(principal.getUser()));
  }

  @GetMapping("/token")
  public Token exchangeCodeToToken(@RequestParam String code) {
    return githubService.exchangeCodeToToken(code);
  }
}
