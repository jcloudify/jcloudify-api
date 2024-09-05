package api.jcloudify.app.service.event;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.model.RefreshEnvBillingInfoRequested;
import api.jcloudify.app.service.LambdaFunctionLogService;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RefreshEnvBillingInfoRequestedService
    implements Consumer<RefreshEnvBillingInfoRequested> {
  private final CloudwatchComponent cloudwatchComponent;
  private final LambdaFunctionLogService lambdaFunctionLogService;

  @Override
  public void accept(RefreshEnvBillingInfoRequested rebirEvent) {
    var computeStackResource =
    var logGroups =
        lambdaFunctionLogService.getLogGroups(
            rebirEvent.getUserId(), rebirEvent.getAppId(), rebirEvent.getEnvId());
    cloudwatchComponent.initiateLogInsightsQuery(
        """
""",
        null, null, List.of());
  }
}
