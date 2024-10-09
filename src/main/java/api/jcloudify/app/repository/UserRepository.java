package api.jcloudify.app.repository;

import api.jcloudify.app.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
  Optional<User> findByGithubId(String githubId);

  boolean existsByEmail(String email);

  boolean existsByGithubId(String githubId);

  List<User> findAll();

  Optional<User> findById(String id);

  List<User> saveAll(List<User> users);
}
