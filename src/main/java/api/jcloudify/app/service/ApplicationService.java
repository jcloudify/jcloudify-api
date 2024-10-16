package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.ApplicationCrupdated;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.repository.jpa.dao.ApplicationDao;
import api.jcloudify.app.repository.model.Application;
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

  public ApplicationService(
      ApplicationRepository repository,
      ApplicationDao dao,
      @Qualifier("DomainApplicationMapper") ApplicationMapper mapper,
      EventProducer<ApplicationCrupdated> applicationCrupdatedEventProducer) {
    this.repository = repository;
    this.dao = dao;
    this.mapper = mapper;
    this.applicationCrupdatedEventProducer = applicationCrupdatedEventProducer;
  }

  @Transactional
  public List<Application> saveApplications(List<ApplicationBase> toSave) {
    List<ApplicationCrupdated> events = new ArrayList<>();
    List<Application> entities = toSave.stream().map(mapper::toDomain).toList();
    for (Application app : entities) {
      String appName = app.getName();
      String id = app.getId();
      boolean existsById = repository.existsById(id);
      if (!existsById && repository.existsByName(appName)) {
        throw new BadRequestException("Application with name=" + appName + " already exists");
      }
      if (existsById) {
        var persisted = getById(id);
        app.setPreviousGithubRepositoryName(persisted.getGithubRepositoryName());
      }
      events.add(toApplicationCrupdatedEvent(app));
    }
    var saved = repository.saveAll(entities);
    applicationCrupdatedEventProducer.accept(events);
    return saved;
  }

  private ApplicationCrupdated toApplicationCrupdatedEvent(Application entity) {
    return ApplicationCrupdated.builder()
        .applicationId(entity.getId())
        .applicationRepoName(entity.getGithubRepositoryName())
        .repoUrl(entity.getGithubRepositoryUrl())
        .installationId(entity.getInstallationId())
        .description(entity.getDescription())
        .repoPrivate(entity.isGithubRepositoryPrivate())
        .previousApplicationRepoName(entity.getPreviousGithubRepositoryName())
        .archived(entity.isArchived())
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

  public List<Application> findAllByUserId(String userId) {
    return repository.findAllByUserId(userId);
  }
}
