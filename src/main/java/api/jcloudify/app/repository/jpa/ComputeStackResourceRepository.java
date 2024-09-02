package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.ComputeStackResource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputeStackResourceRepository
    extends JpaRepository<ComputeStackResource, String> {
  List<ComputeStackResource> findAllByEnvironmentIdOrderByCreationDatetimeDesc(
      String environmentId);
}
