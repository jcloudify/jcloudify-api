package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.ComputeStackResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComputeStackResourceRepository extends JpaRepository<ComputeStackResource, String> {
    List<ComputeStackResource> findAllByEnvironmentIdOrderByCreationDatetimeDesc(String environmentId);
}
