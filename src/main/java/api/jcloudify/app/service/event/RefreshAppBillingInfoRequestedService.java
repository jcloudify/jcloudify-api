package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.RefreshAppBillingInfoRequested;
import api.jcloudify.app.endpoint.event.model.RefreshEnvBillingInfoRequested;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.EnvironmentService;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RefreshAppBillingInfoRequestedService
    implements Consumer<RefreshAppBillingInfoRequested> {
  private final EnvironmentService environmentService;
  private final EventProducer<RefreshEnvBillingInfoRequested> eventProducer;

  @Override
  public void accept(RefreshAppBillingInfoRequested rabifEvent) {
    var envs = environmentService.findAllByApplicationId(rabifEvent.getAppId());
    var events = envs.stream().map(e -> toRefreshEnvBillingInfoRequested(e, rabifEvent)).toList();
    eventProducer.accept(events);
  }

  private static RefreshEnvBillingInfoRequested toRefreshEnvBillingInfoRequested(
      Environment environment, RefreshAppBillingInfoRequested parent) {
    return new RefreshEnvBillingInfoRequested(
        environment.getId(), parent.getUserId(), parent.getAppId(), parent);
  }
}
