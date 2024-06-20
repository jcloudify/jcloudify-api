package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, String> {}
