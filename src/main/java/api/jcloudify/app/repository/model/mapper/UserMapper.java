package api.jcloudify.app.repository.model.mapper;

import static api.jcloudify.app.endpoint.rest.security.model.UserRole.USER;
import static api.jcloudify.app.service.pricing.PricingMethod.TEN_MICRO;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.security.model.UserRole;
import api.jcloudify.app.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.kohsuke.github.GHMyself;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public static final UserRole[] CREATE_USER_DEFAULT_ROLE = {USER};
  private final List<String> betaTesterGhUsernames;

  @SneakyThrows
  public UserMapper(@Value("${beta.users}") String betaTesterGhUsernames, ObjectMapper om) {
    this.betaTesterGhUsernames = om.readValue(betaTesterGhUsernames, new TypeReference<>() {});
  }

  private boolean isBetaTester(String githubUsername) {
    return betaTesterGhUsernames.contains(githubUsername);
  }

  public User toModel(CreateUser toCreate, GHMyself githubUser, String stripeId) {
    String githubUsername = githubUser.getLogin();
    var isBetaTester = isBetaTester(githubUsername);
    return User.builder()
        .firstName(toCreate.getFirstName())
        .lastName(toCreate.getLastName())
        .githubId(String.valueOf(githubUser.getId()))
        .email(toCreate.getEmail())
        .username(githubUsername)
        .avatar(githubUser.getAvatarUrl())
        .roles(CREATE_USER_DEFAULT_ROLE)
        .pricingMethod(TEN_MICRO)
        .stripeId(stripeId)
        .betaTester(isBetaTester)
        .build();
  }

  public User toModel(api.jcloudify.app.repository.model.User entity) {
    String githubUsername = entity.getUsername();
    var isBetaTester = isBetaTester(githubUsername);
    return User.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .githubId(entity.getGithubId())
        .email(entity.getEmail())
        .username(entity.getUsername())
        .avatar(entity.getAvatar())
        .roles(entity.getRoles())
        .pricingMethod(entity.getPricingMethod())
        .stripeId(entity.getStripeId())
        .betaTester(isBetaTester)
        .build();
  }

  public api.jcloudify.app.repository.model.User toEntity(User model) {
    return api.jcloudify.app.repository.model.User.builder()
        .id(model.getId())
        .firstName(model.getFirstName())
        .lastName(model.getLastName())
        .githubId(model.getGithubId())
        .email(model.getEmail())
        .username(model.getUsername())
        .avatar(model.getAvatar())
        .roles(model.getRoles())
        .pricingMethod(model.getPricingMethod())
        .stripeId(model.getStripeId())
        .build();
  }
}
