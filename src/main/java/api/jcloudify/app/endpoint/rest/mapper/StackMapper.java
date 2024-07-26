package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.endpoint.rest.model.StackResourceStatusType;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudformation.model.ResourceStatus;

import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UNKNOWN_TO_SDK_VERSION;

@Component
@AllArgsConstructor
@Slf4j
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
        .stackType(domain.getType())
        .creationDatetime(domain.getCreationDatetime())
        .updateDatetime(domain.getUpdateDatetime());
  }

  public StackEvent toRest(software.amazon.awssdk.services.cloudformation.model.StackEvent domain) {
    return new StackEvent()
            .eventId(domain.eventId())
            .logicalResourceId(domain.logicalResourceId())
            .resourceType(domain.resourceType())
            .resourceStatus(toRestStackEventStatusType(domain.resourceStatus()))
            .timestamp(domain.timestamp())
            .statusMessage(domain.resourceStatusReason());
  }

  private StackResourceStatusType toRestStackEventStatusType(ResourceStatus domain) {
    try {
      if (domain == ResourceStatus.UNKNOWN_TO_SDK_VERSION) {
        return UNKNOWN_TO_SDK_VERSION;
      }
      return StackResourceStatusType.valueOf(domain.toString());
    } catch (IllegalArgumentException e) {
      log.error("No enum constant for value: {}", domain.getClass());
      throw new InternalServerErrorException(e);
    }
  }
}
