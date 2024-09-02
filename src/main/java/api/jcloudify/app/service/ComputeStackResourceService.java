package api.jcloudify.app.service;

import api.jcloudify.app.repository.jpa.ComputeStackResourceRepository;
import api.jcloudify.app.repository.model.ComputeStackResource;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ComputeStackResourceService {
  private final ComputeStackResourceRepository repository;

  public List<ComputeStackResource> findAllByEnvironmentId(String environmentId) {
    return repository.findAllByEnvironmentIdOrderByCreationDatetimeDesc(environmentId);
  }

  public ComputeStackResource save(ComputeStackResource resource) {
    return repository.save(resource);
  }
}
