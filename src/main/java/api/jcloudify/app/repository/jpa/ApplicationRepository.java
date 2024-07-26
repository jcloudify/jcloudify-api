package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.Application;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {
  Optional<Application> findByIdAndUserId(String id, String userId);
}
