package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.model.User;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.model.exception.ForbiddenException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.UserRepository;
import api.jcloudify.app.repository.model.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.kohsuke.github.GHMyself;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final GithubComponent githubComponent;
  private final UserMapper mapper;

  private final StripeService stripeService;

  public List<User> createUsers(List<CreateUser> toCreate) {
    List<User> toSave = toCreate.stream().map(this::createUserFrom).toList();
    return repository.saveAll(toSave);
  }

  public List<User> getAllUsers() {
    return repository.findAll();
  }

  public User getUserById(String userId) {
    return findById(userId)
        .orElseThrow(
            () -> new NotFoundException("The user identified by id " + userId + " is not found"));
  }

  public List<User> findAll() {
    return repository.findAll();
  }

  public Optional<User> findById(String userId) {
    return repository.findById(userId);
  }

  private User createUserFrom(CreateUser createUser) {
    String stripeName = createUser.getFirstName() + " " + createUser.getLastName();
    GHMyself githubUser = getUserByToken(createUser.getToken());
    String customerId = stripeService.createCustomer(stripeName, createUser.getEmail()).getId();
    User user = mapper.toModel(createUser, githubUser, customerId);
    if (repository.existsByEmail(user.getEmail()))
      throw new BadRequestException("An account with the same email already exists");
    if (repository.existsByGithubId(user.getGithubId()))
      throw new BadRequestException("An account with the same github id already exists");
    return user;
  }

  private GHMyself getUserByToken(String token) {
    return githubComponent
        .getCurrentUserByToken(token)
        .orElseThrow(() -> new ForbiddenException("Invalid token"));
  }

  public User findByGithubUserId(String githubUserId) {
    return repository
        .findByGithubId(githubUserId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "The user identified by the github id " + githubUserId + " is not found"));
  }
}
