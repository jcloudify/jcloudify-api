package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.ApplicationCrupdated;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.repository.jpa.dao.ApplicationDao;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.mapper.ApplicationMapper;
import java.util.ArrayList;
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
  private final EventProducer<ApplicationCrupdated> applicationCrupdatedEventProducer;
  private final EnvironmentService environmentService;

  public ApplicationService(
      ApplicationRepository repository,
      ApplicationDao dao,
      @Qualifier("DomainApplicationMapper") ApplicationMapper mapper,
      EventProducer<ApplicationCrupdated> applicationCrupdatedEventProducer,
      EnvironmentService environmentService) {
    this.repository = repository;
    this.dao = dao;
    this.mapper = mapper;
    this.applicationCrupdatedEventProducer = applicationCrupdatedEventProducer;
    this.environmentService = environmentService;
  }

  @Transactional
  public List<Application> saveApplications(List<ApplicationBase> toSave) {
    List<ApplicationCrupdated> events = new ArrayList<>();
    List<Application> entities = toSave.stream().map(mapper::toDomain).toList();
    for (Application app : entities) {
      if (repository.existsById(app.getId())) {
        var persisted = getById(app.getId());
        app.setPreviousGithubRepositoryName(persisted.getGithubRepositoryName());
        if (app.isArchived()) {
          archiveApplication(app.getId());
        }
      }
      events.add(toApplicationCrupdatedEvent(app));
    }
    var saved = repository.saveAll(entities);
    applicationCrupdatedEventProducer.accept(events);
    return saved;
  }

  private void archiveApplication(String applicationId) {
    List<Environment> applicationEnvironments =
        environmentService.findAllByApplicationId(applicationId);
    applicationEnvironments.forEach(env -> env.setArchived(true));
    environmentService.crupdateEnvironments(applicationId, applicationEnvironments);
  }

  private ApplicationCrupdated toApplicationCrupdatedEvent(Application entity) {
    return ApplicationCrupdated.builder()
        .applicationId(entity.getId())
        .applicationRepoName(entity.getGithubRepositoryName())
        .repoUrl(entity.getGithubRepositoryUrl())
        .installationId(entity.getInstallationId())
        .description(entity.getDescription())
        .isRepoPrivate(entity.isGithubRepositoryPrivate())
        .previousApplicationRepoName(entity.getPreviousGithubRepositoryName())
        .isArchived(entity.isArchived())
        .build();
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

  public Application findByRepositoryId(String repositoryId) {
    return repository
        .findByGithubRepositoryId(repositoryId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Application identified by repository id=" + repositoryId + " not found"));
  }

  public Application getById(String id, String userId) {
    return repository
        .findByIdAndUserId(id, userId)
        .orElseThrow(
            () -> new NotFoundException("Application identified by id=" + id + " not found"));
  }
}
