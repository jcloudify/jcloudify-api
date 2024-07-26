package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.repository.jpa.dao.ApplicationDao;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.mapper.ApplicationMapper;
import api.jcloudify.app.service.github.GithubService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ApplicationService {
  private final ApplicationRepository repository;
  private final ApplicationDao dao;
  private final ApplicationMapper mapper;
  private final GithubService githubService;

  public ApplicationService(
      ApplicationRepository repository,
      ApplicationDao dao,
      @Qualifier("DomainApplicationMapper") ApplicationMapper mapper,
      GithubService githubService) {
    this.repository = repository;
    this.dao = dao;
    this.mapper = mapper;
    this.githubService = githubService;
  }

  @Transactional
  public List<Application> saveApplications(List<ApplicationBase> toSave) {
    /* List<Application> createdApplications = new ArrayList<>();
    List<Application> updatedApplications = new ArrayList<>();
    for (Application app : toSave.stream().map(mapper::toDomain).toList()) {
      if (repository.existsById(app.getId())) {
        var persisted = getById(app.getId());
        app.setPreviousGithubRepositoryName(persisted.getGithubRepositoryName());
        updatedApplications.add(repository.save(app));
      } else {
        createdApplications.add(repository.save(app));
      }
    }
    Principal principal = AuthProvider.getPrincipal();
    String token = principal.getBearer();
    String githubUsername = principal.getUsername();
    var githubCreatedApplications = createRepoFrom(createdApplications, token);
    var githubUpdatedApplications = updateRepoFor(updatedApplications, token, githubUsername);
    var result = new ArrayList<>(githubCreatedApplications);
    result.addAll(githubUpdatedApplications);*/
    return repository.saveAll(toSave.stream().map(mapper::toDomain).toList());
  }

  private List<Application> createRepoFrom(List<Application> toCreate, String token) {
    toCreate.forEach(
        app -> {
          var url = githubService.createRepoFor(app, token);
          app.setRepoHttpUrl(url.toString());
        });
    return toCreate;
  }

  private List<Application> updateRepoFor(
      List<Application> toUpdate, String token, String githubUsername) {
    toUpdate.forEach(
        app -> {
          var url = githubService.updateRepoFor(app, token, githubUsername);
          app.setRepoHttpUrl(url.toString());
        });
    return toUpdate;
  }

  public Application getById(String id) {
    return findById(id)
        .orElseThrow(
            () -> new NotFoundException("Application identified by id=" + id + " not found"));
  }

  public Optional<Application> findById(String id) {
    return repository.findById(id);
  }

  public Page<Application> findAllByCriteria(
      String userId, String name, PageFromOne pageFromOne, BoundedPageSize boundedPageSize) {
    var data =
        dao.findAllByCriteria(
            userId, name, PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue()));
    return new Page<>(pageFromOne, boundedPageSize, data);
  }

  public Application getById(String id, String userId) {
    return repository
        .findByIdAndUserId(id, userId)
        .orElseThrow(
            () -> new NotFoundException("Application identified by id=" + id + " not found"));
  }
}
