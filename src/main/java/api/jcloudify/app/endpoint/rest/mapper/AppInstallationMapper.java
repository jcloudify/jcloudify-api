package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.AppInstallation;
import org.springframework.stereotype.Component;

@Component
public class AppInstallationMapper {
  public AppInstallation toRest(api.jcloudify.app.repository.model.AppInstallation domain) {
    return new AppInstallation()
        .owner(domain.getOwnerGithubLogin())
        .ghInstallationId(domain.getGhId())
        .id(domain.getId())
        .isOrg(domain.isOrg());
  }

  public api.jcloudify.app.repository.model.AppInstallation toDomain(
      String userId, AppInstallation rest) {
    return api.jcloudify.app.repository.model.AppInstallation.builder()
        .id(rest.getId())
        .ownerGithubLogin(rest.getOwner())
        .ghId(rest.getGhInstallationId())
        .isOrg(rest.getIsOrg())
        .userId(userId)
        .build();
  }
}
