package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.EnvironmentBuild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentBuildRepository extends JpaRepository<EnvironmentBuild, String> {}
