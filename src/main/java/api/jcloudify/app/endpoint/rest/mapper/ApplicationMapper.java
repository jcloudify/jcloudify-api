package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.repository.model.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {
  public ApplicationBase toRest(Application domain) {
    return new ApplicationBase()
        .id(domain.getId())
        .name(domain.getName())
        .archived(domain.isArchived())
        .userId(domain.getUserId());
  }
}
