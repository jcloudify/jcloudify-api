package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.Application;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {
  Optional<Application> findByIdAndUserId(String id, String userId);

  @Modifying
  @Query("update Application a set a.repoHttpUrl = ?2 where a.id = ?1")
  void updateApplicationRepoUrl(String id, String repoUrl);
}
