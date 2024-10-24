package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, String> {
  Optional<User> findByGithubId(String githubId);

  boolean existsByEmail(String email);

  boolean existsByGithubId(String githubId);
}
