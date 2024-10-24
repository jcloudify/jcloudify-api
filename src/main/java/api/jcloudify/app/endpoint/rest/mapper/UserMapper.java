package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("restMapper")
@AllArgsConstructor
public class UserMapper {
  private final UserRoleMapper userRoleMapper;

  public User toRest(api.jcloudify.app.model.User domain) {
    return new User()
        .id(domain.getId())
        .username(domain.getUsername())
        .email(domain.getEmail())
        .githubId(domain.getGithubId())
        .firstName(domain.getFirstName())
        .lastName(domain.getLastName())
        .role(userRoleMapper.toRest(domain.getRoles()[0]))
        .avatar(domain.getAvatar())
        .stripeId(domain.getStripeId())
        .isBetaTester(domain.isBetaTester());
  }
}
