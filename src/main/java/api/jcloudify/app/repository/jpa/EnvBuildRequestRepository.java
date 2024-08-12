package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.EnvBuildRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvBuildRequestRepository extends JpaRepository<EnvBuildRequest, String> {}
