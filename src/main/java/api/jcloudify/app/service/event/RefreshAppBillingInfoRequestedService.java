package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.RefreshAppBillingInfoRequested;
import api.jcloudify.app.service.EnvironmentService;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RefreshAppBillingInfoRequestedService
    implements Consumer<RefreshAppBillingInfoRequested> {
  private final EnvironmentService environmentService;
  @Override
  public void accept(RefreshAppBillingInfoRequested refreshAppBillingInfoRequested) {
  }
}
