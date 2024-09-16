package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.Application;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {
  Optional<Application> findByIdAndUserId(String id, String userId);

  Optional<Application> findByGithubRepositoryId(String repositoryId);

  @Modifying
  @Query(
      """
      update Application a set a.githubRepositoryUrl = ?2,
      a.githubRepositoryId = ?3  where a.id = ?1""")
  void updateApplicationRepoUrl(String id, String githubRepositoryUrl, String githubRepositoryId);

  List<Application> findAllByUserId(String userId);
}
