package api.jcloudify.app.repository.model.mapper;

import static api.jcloudify.app.repository.model.enums.UserRole.USER;
import static api.jcloudify.app.service.pricing.PricingMethod.TEN_MICRO;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.repository.model.enums.UserRole;
import org.kohsuke.github.GHMyself;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public static final UserRole[] CREATE_USER_DEFAULT_ROLE = {USER};

  public User toModel(CreateUser toCreate, GHMyself githubUser, String stripeId) {
    return User.builder()
        .firstName(toCreate.getFirstName())
        .lastName(toCreate.getLastName())
        .githubId(String.valueOf(githubUser.getId()))
        .email(toCreate.getEmail())
        .username(githubUser.getLogin())
        .avatar(githubUser.getAvatarUrl())
        .roles(CREATE_USER_DEFAULT_ROLE)
        .pricingMethod(TEN_MICRO)
        .stripeId(stripeId)
        .build();
  }
}
