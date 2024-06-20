package api.jcloudify.app.service;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvironmentRepository;
import api.jcloudify.app.repository.model.Environment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EnvironmentService {
  private final EnvironmentRepository repository;

  public Environment getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("Environment identified by id " + id + " not found"));
  }
}
