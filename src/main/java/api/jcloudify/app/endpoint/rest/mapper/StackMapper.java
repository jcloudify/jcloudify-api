package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StackMapper {
  private final ApplicationMapper applicationMapper;
  private final EnvironmentMapper environmentMapper;

  public Stack toRest(
      api.jcloudify.app.repository.model.Stack domain,
      Application application,
      Environment environment) {
    return new Stack()
        .id(domain.getId())
        .name(domain.getName())
        .cfStackId(domain.getCfStackId())
        .application(applicationMapper.toBaseRest(application))
        .environment(environmentMapper.toRest(environment))
        .stackType(domain.getType());
  }
}
