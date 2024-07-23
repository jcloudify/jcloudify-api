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
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplicationService {
  private final ApplicationRepository repository;
  private final ApplicationDao dao;
  private final ApplicationMapper mapper;

  public ApplicationService(
      ApplicationRepository repository,
      ApplicationDao dao,
      @Qualifier("DomainApplicationMapper") ApplicationMapper mapper) {
    this.repository = repository;
    this.dao = dao;
    this.mapper = mapper;
  }

  public List<Application> saveApplications(List<ApplicationBase> toSave) {
    return repository.saveAll(toSave.stream().map(mapper::toDomain).toList());
  }

  public Application getById(String id) {
    return findById(id)
            .orElseThrow(() -> new NotFoundException("Application identified by id=" + id + " not found"));
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
}
