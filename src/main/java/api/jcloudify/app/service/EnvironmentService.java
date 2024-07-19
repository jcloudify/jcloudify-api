package api.jcloudify.app.service;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvironmentRepository;
import api.jcloudify.app.repository.model.Environment;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EnvironmentService {
  private final EnvironmentRepository repository;

  public List<Environment> findAllByApplicationId(String applicationId) {
    return repository.findAllByApplicationId(applicationId);
  }

  public Environment getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("Environment identified by id " + id + " not found"));
  }

  public List<Environment> crupdateEnvironments(
      String applicationId, List<Environment> environments) {
    return repository.saveAll(environments);
  }
}
