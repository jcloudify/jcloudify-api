package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.mapper.ApplicationMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ApplicationService {
  private final ApplicationRepository repository;

  @Qualifier("DomainApplicationMapper")
  private final ApplicationMapper mapper;

  public List<Application> saveApplications(List<ApplicationBase> toSave) {
    return repository.saveAll(toSave.stream().map(mapper::toDomain).toList());
  }

  public Application getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("Application identified by id=" + id + " not found"));
  }
}
