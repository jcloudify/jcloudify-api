package api.jcloudify.app.endpoint.rest.mapper;

import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.UNKNOWN;
import static java.lang.Boolean.TRUE;

import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironment;
import api.jcloudify.app.endpoint.rest.model.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentMapper {
  public Environment toRest(api.jcloudify.app.repository.model.Environment domain) {
    return new Environment()
        .id(domain.getId())
        .state(domain.getState())
        .environmentType(domain.getEnvironmentType());
  }

  public api.jcloudify.app.repository.model.Environment toDomain(
      String applicationId, CrupdateEnvironment rest) {
    return api.jcloudify.app.repository.model.Environment.builder()
        .id(rest.getId())
        .archived(TRUE.equals(rest.getArchived()))
        .applicationId(applicationId)
        .state(UNKNOWN)
        .environmentType(rest.getEnvironmentType())
        .build();
  }
}
