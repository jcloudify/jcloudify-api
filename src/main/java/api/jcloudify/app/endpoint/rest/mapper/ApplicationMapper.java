package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.service.EnvironmentService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("RestApplicationMapper")
@AllArgsConstructor
public class ApplicationMapper {
  private final EnvironmentService environmentService;
  private final EnvironmentMapper environmentMapper;

  public ApplicationBase toBaseRest(api.jcloudify.app.repository.model.Application domain) {
    return new ApplicationBase()
        .id(domain.getId())
        .name(domain.getName())
        .archived(domain.isArchived())
        .githubRepository(domain.getGithubRepository())
        .userId(domain.getUserId());
  }

  public Application toRest(api.jcloudify.app.repository.model.Application domain) {
    return new api.jcloudify.app.endpoint.rest.model.Application()
        .id(domain.getId())
        .name(domain.getName())
        .githubRepository(domain.getGithubRepository())
        .archived(domain.isArchived())
        .userId(domain.getUserId())
        .creationDatetime(domain.getCreationDatetime())
        .environments(getEnvironmentsByApplicationId(domain.getId()));
  }

  private List<Environment> getEnvironmentsByApplicationId(String applicationId) {
    return this.environmentService.findAllByApplicationId(applicationId).stream()
        .map(environmentMapper::toRest)
        .toList();
  }
}
