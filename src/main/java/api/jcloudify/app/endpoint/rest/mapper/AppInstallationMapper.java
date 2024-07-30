package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.CreateGithubAppInstallation;
import api.jcloudify.app.endpoint.rest.model.GithubAppInstallation;
import api.jcloudify.app.service.github.GithubService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AppInstallationMapper {
  private final GithubService githubService;

  public GithubAppInstallation toRest(api.jcloudify.app.repository.model.AppInstallation domain) {
    return new GithubAppInstallation()
        .owner(domain.getOwnerGithubLogin())
        .ghInstallationId(domain.getGhId())
        .id(domain.getId())
        .type(domain.getType())
        .ghAvatarUrl(domain.getAvatarUrl());
  }

  public api.jcloudify.app.repository.model.AppInstallation toDomain(
      String userId, CreateGithubAppInstallation rest) {
    var app = githubService.getInstallationByGhId(rest.getGhInstallationId());
    String ownerGithubLogin = app.ownerGithubLogin();
    return api.jcloudify.app.repository.model.AppInstallation.builder()
        .id(rest.getId())
        .ownerGithubLogin(ownerGithubLogin)
        .ghId(rest.getGhInstallationId())
        .type(app.type())
        .avatarUrl(app.avatarUrl())
        .userId(userId)
        .build();
  }
}
