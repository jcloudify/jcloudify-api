package api.jcloudify.app.service.event;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.model.RefreshEnvBillingInfoRequested;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RefreshEnvBillingInfoRequestedService
    implements Consumer<RefreshEnvBillingInfoRequested> {
  private CloudwatchComponent cloudwatchComponent;
  private LogGroups

  @Override
  public void accept(RefreshEnvBillingInfoRequested refreshEnvBillingInfoRequested) {
    cloudwatchComponent.initiateLogInsightsQuery(
        """
""",
        null, null, List.of());
  }
}
