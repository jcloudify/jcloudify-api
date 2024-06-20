package api.jcloudify.app.endpoint.rest.mapper;

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
}
