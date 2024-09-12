package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvironmentDeploymentRepository;
import api.jcloudify.app.repository.jpa.dao.EnvironmentDeploymentDao;
import api.jcloudify.app.repository.model.AppEnvironmentDeployment;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppEnvironmentDeploymentService {
  private final EnvironmentDeploymentRepository repository;
  private final EnvironmentDeploymentDao dao;

  public AppEnvironmentDeployment getById(String id) {
    return findById(id)
        .orElseThrow(
            () -> new NotFoundException("AppEnvironmentDeployment.Id = " + id + " not found."));
  }

  public Optional<AppEnvironmentDeployment> findById(String id) {
    return repository.findById(id);
  }

  public Page<AppEnvironmentDeployment> findAllByCriteria(
      String userId,
      String appId,
      EnvironmentType envType,
      Instant startDatetime,
      Instant endDatetime,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var data =
        dao.findAllByCriteria(
            userId,
            appId,
            envType,
            startDatetime,
            endDatetime,
            PageRequest.of(page.getValue() - 1, pageSize.getValue()));
    return new Page<>(page, pageSize, data);
  }
}
