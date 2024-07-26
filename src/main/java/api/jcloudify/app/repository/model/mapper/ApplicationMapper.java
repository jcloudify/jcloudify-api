package api.jcloudify.app.repository.model.mapper;

import static java.lang.Boolean.TRUE;

import api.jcloudify.app.endpoint.rest.model.GithubRepository;
import api.jcloudify.app.repository.model.Application;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("DomainApplicationMapper")
@AllArgsConstructor
public class ApplicationMapper {

  public Application toDomain(api.jcloudify.app.endpoint.rest.model.ApplicationBase rest) {
    GithubRepository githubRepository = rest.getGithubRepository();
    return Application.builder()
        .id(rest.getId())
        .name(rest.getName())
        .githubRepositoryName(githubRepository.getName())
        .isGithubRepositoryPrivate(TRUE.equals(githubRepository.getIsPrivate()))
        .userId(rest.getUserId())
        .archived(rest.getArchived())
        .description(githubRepository.getDescription())
        .installationId(githubRepository.getInstallationId())
        .build();
  }
}
