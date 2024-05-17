package api.jcloudify.app.service;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.UserRepository;
import api.jcloudify.app.repository.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;

  public User save(User user) {
    return repository.save(user);
  }

  public User getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("The user identified by the id " + id + " is not found"));
  }
}
