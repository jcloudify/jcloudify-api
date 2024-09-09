package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.RefreshUserBillingInfoRequested;
import api.jcloudify.app.endpoint.event.model.RefreshUsersBillingInfoTriggered;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.service.UserService;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class RefreshUsersBillingInfoTriggeredService
    implements Consumer<RefreshUsersBillingInfoTriggered> {
  private final UserService userService;
  private final EventProducer<RefreshUserBillingInfoRequested> eventProducer;

  @Override
  public void accept(RefreshUsersBillingInfoTriggered refreshUsersBillingInfoTriggered) {
    List<User> allUsers = userService.findAll();
    log.info("initiating billings refresh for {} users", allUsers.size());
    eventProducer.accept(
        allUsers.stream()
            .map(u -> toRefreshUserBillingInfoRequested(u, refreshUsersBillingInfoTriggered))
            .toList());
  }

  private static RefreshUserBillingInfoRequested toRefreshUserBillingInfoRequested(
      User user, RefreshUsersBillingInfoTriggered parent) {
    return new RefreshUserBillingInfoRequested(user.getId(), parent, user.getPricingMethod());
  }
}
