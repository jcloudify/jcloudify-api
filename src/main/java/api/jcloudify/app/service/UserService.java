package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.model.exception.ForbiddenException;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.UserRepository;
import api.jcloudify.app.repository.model.User;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.kohsuke.github.GHMyself;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final GithubComponent githubComponent;

  public List<User> createUsers(List<CreateUser> toCreate) {
    return toCreate.stream()
        .map(
            user -> {
              GHMyself githubUser =
                  githubComponent
                      .getCurrentUserByToken(user.getToken())
                      .orElseThrow(() -> new ForbiddenException("Invalid token"));

              User.UserBuilder userBuilder = User.builder();

              userBuilder.firstName(user.getFirstName()).lastName(user.getLastName());

              try {
                userBuilder
                    .email(githubUser.getEmail())
                    .githubId(String.valueOf(githubUser.getId()))
                    .email(githubUser.getEmail())
                    .username(githubUser.getName());
              } catch (IOException e) {
                throw new InternalServerErrorException(e);
              }
              return repository.save(userBuilder.build());
            })
        .toList();
  }

  public User findByEmail(String email) {
    return repository
        .findByEmail(email)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "The user identified by the email " + email + " is not found"));
  }
}
