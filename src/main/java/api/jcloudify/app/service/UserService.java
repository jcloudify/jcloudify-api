package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.UserBase;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.UserRepository;
import api.jcloudify.app.repository.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;

  public User registerUser(UserBase toRegister) {
    String email = toRegister.getEmail();
    if (repository.existsByEmail(email)) {
      throw new BadRequestException("The username " + email + " is used by an user account");
    }
    User toCreate =
        User.builder()
            .firstName(toRegister.getFirstName())
            .lastName(toRegister.getLastName())
            .email(email)
            .username(toRegister.getUsername())
            .build();
    return repository.save(toCreate);
  }

  public User getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("The user identified by the id " + id + " is not found"));
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
