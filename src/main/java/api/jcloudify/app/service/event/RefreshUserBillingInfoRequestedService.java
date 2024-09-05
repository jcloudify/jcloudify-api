package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.RefreshAppBillingInfoRequested;
import api.jcloudify.app.endpoint.event.model.RefreshUserBillingInfoRequested;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.service.ApplicationService;
import java.time.Instant;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RefreshUserBillingInfoRequestedService
    implements Consumer<RefreshUserBillingInfoRequested> {
  private final EventProducer<RefreshAppBillingInfoRequested> eventProducer;
  private final ApplicationService applicationService;

  @Override
  public void accept(RefreshUserBillingInfoRequested refreshUserBillingInfoRequested) {
    var requestTime = Instant.now();
    var apps = applicationService.findAllByUserId(refreshUserBillingInfoRequested.getUserId());
    eventProducer.accept(
        apps.stream()
            .map(
                a ->
                    toRefreshAppBillingInfoRequested(
                        a, refreshUserBillingInfoRequested, requestTime))
            .toList());
  }

  private RefreshAppBillingInfoRequested toRefreshAppBillingInfoRequested(
      Application application, RefreshUserBillingInfoRequested parent, Instant requestTime) {
    return new RefreshAppBillingInfoRequested(
        application.getUserId(),
        application.getId(),
        requestTime,
        parent,
        parent.getPricingMethod());
  }
}
