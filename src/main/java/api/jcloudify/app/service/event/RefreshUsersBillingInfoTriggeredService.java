package api.jcloudify.app.service.event;

import static java.util.UUID.randomUUID;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.RefreshUserBillingInfoRequested;
import api.jcloudify.app.endpoint.event.model.RefreshUsersBillingInfoTriggered;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.service.UserService;
import java.time.Instant;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RefreshUsersBillingInfoTriggeredService
    implements Consumer<RefreshUsersBillingInfoTriggered> {
  private final UserService userService;
  private final EventProducer<RefreshUserBillingInfoRequested> eventProducer;

  @Override
  public void accept(RefreshUsersBillingInfoTriggered refreshUsersBillingInfoTriggered) {
    final var requestTime = Instant.now();
    // not persisted ID, will only be used for logging purposes
    final var requestId = randomUUID();
    eventProducer.accept(
        userService.findAll().stream()
            .map(u -> toRefreshUserBillingInfoRequested(u, refreshUsersBillingInfoTriggered))
            .toList());
  }

  private static RefreshUserBillingInfoRequested toRefreshUserBillingInfoRequested(
      User user, RefreshUsersBillingInfoTriggered parent) {
    return new RefreshUserBillingInfoRequested(user.getId(), parent, user.getPricingMethod());
  }
}
