package api.jcloudify.app.aws.cloudwatch;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Component
@AllArgsConstructor
public class CloudwatchComponent {
  private final CloudWatchClient cloudWatchClient;

  public void getMetrics(List<String> logGroupNames) {
    //cloudWatchClient.getMetricDataPaginator(req -> req.metricDataQueries(req2 -> req2.));
  }
}
