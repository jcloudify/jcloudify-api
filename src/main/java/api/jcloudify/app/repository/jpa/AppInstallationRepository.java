package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.AppInstallation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppInstallationRepository extends JpaRepository<AppInstallation, String> {
  List<AppInstallation> findAllByUserId(String userId);
}
