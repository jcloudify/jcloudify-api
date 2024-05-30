package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.model.exception.ForbiddenException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.UserRepository;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.repository.model.mapper.UserMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.kohsuke.github.GHMyself;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final GithubComponent githubComponent;
  private final UserMapper mapper;

  public List<User> createUsers(List<CreateUser> toCreate) {
    List<User> toSave = toCreate.stream().map(this::createUserFrom).toList();
    return repository.saveAll(toSave);
  }

  private User createUserFrom(CreateUser user) {
    GHMyself githubUser = getUserByToken(user.getToken());
    return mapper.toModel(user, githubUser);
  }

  private GHMyself getUserByToken(String token) {
    return githubComponent
        .getCurrentUserByToken(token)
        .orElseThrow(() -> new ForbiddenException("Invalid token"));
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
