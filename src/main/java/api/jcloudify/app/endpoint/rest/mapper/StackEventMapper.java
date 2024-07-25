package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.endpoint.rest.model.StackResourceStatusType;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudformation.model.ResourceStatus;

import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UNKNOWN_TO_SDK_VERSION;

@Component
@Slf4j
public class StackEventMapper {
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
