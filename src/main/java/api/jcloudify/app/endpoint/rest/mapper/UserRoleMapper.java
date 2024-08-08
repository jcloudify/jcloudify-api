package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.User;
import api.jcloudify.app.endpoint.rest.security.model.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {
  public User.RoleEnum toRest(UserRole domain) {
    return switch (domain) {
      case ADMIN -> User.RoleEnum.ADMIN;
      case USER -> User.RoleEnum.USER;
    };
  }
}
