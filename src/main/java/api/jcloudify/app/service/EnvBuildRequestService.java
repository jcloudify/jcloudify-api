package api.jcloudify.app.service;

import api.jcloudify.app.repository.jpa.EnvBuildRequestRepository;
import api.jcloudify.app.repository.model.EnvBuildRequest;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvBuildRequestService {
  private final EnvBuildRequestRepository repository;

  public EnvBuildRequest save(EnvBuildRequest envBuildRequest) {
    return repository.save(envBuildRequest);
  }

  public Optional<EnvBuildRequest> findById(String id) {
    return repository.findById(id);
  }

  public boolean existsById(String id) {
    return repository.existsById(id);
  }
}
