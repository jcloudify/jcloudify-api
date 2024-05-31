package api.jcloudify.app.repository.model.mapper;

import static api.jcloudify.app.repository.model.enums.UserRole.USER;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.model.exception.ForbiddenException;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.repository.model.enums.UserRole;
import lombok.AllArgsConstructor;
import org.kohsuke.github.GHMyself;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {
  private final GithubComponent githubComponent;

  public User toModel(CreateUser toCreate, GHMyself githubUser) {
    UserRole[] roles = {USER};
    return User.builder()
        .firstName(toCreate.getFirstName())
        .lastName(toCreate.getLastName())
        .githubId(String.valueOf(githubUser.getId()))
        .email(getEmailByToken(toCreate.getToken()))
        .username(githubUser.getLogin())
        .roles(roles)
        .build();
  }

  private String getEmailByToken(String token) {
    return githubComponent
        .getEmailByToken(token)
        .orElseThrow(() -> new ForbiddenException("Email does not exists"));
  }
}
