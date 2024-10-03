package api.jcloudify.app.repository.impl;

import api.jcloudify.app.model.User;
import api.jcloudify.app.repository.UserRepository;
import api.jcloudify.app.repository.jpa.UserJpaRepository;
import api.jcloudify.app.repository.model.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
class UserRepositoryImpl implements UserRepository {
  private final UserJpaRepository jpaRepository;
  private final UserMapper mapper;

  @Override
  public Optional<User> findByGithubId(String githubId) {
    return jpaRepository.findByGithubId(githubId).map(mapper::toModel);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByGithubId(String githubId) {
    return jpaRepository.existsByGithubId(githubId);
  }

  @Override
  public List<User> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toModel).toList();
  }

  @Override
  public Optional<User> findById(String id) {
    return jpaRepository.findById(id).map(mapper::toModel);
  }

  @Override
  public List<User> saveAll(List<User> users) {
    return jpaRepository.saveAll(users.stream().map(mapper::toEntity).toList()).stream()
        .map(mapper::toModel)
        .toList();
  }
}
