package api.jcloudify.app.repository.model.mapper;

import api.jcloudify.app.repository.model.Application;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("DomainApplicationMapper")
@AllArgsConstructor
public class ApplicationMapper {

  public Application toDomain(api.jcloudify.app.endpoint.rest.model.ApplicationBase rest) {
    return Application.builder()
        .id(rest.getId())
        .name(rest.getName())
        .githubRepository(rest.getGithubRepository())
        .userId(rest.getUserId())
        .archived(rest.getArchived())
        .build();
  }
}
