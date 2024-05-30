package api.jcloudify.app.repository.model.mapper;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.repository.model.User;
import java.io.IOException;
import org.kohsuke.github.GHMyself;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toModel(CreateUser toCreate, GHMyself githubUser) {
    try {
      return User.builder()
          .firstName(toCreate.getFirstName())
          .lastName(toCreate.getLastName())
          .githubId(String.valueOf(githubUser.getId()))
          .email(githubUser.getEmail())
          .username(githubUser.getName())
          .build();
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }
}
