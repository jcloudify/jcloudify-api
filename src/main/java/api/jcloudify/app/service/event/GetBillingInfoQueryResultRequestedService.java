package api.jcloudify.app.service.event;

import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.COMPLETE;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.FAILED;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.RUNNING;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.model.GetBillingInfoQueryResultRequested;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResultField;

@Component
@AllArgsConstructor
public class GetBillingInfoQueryResultRequestedService
    implements Consumer<GetBillingInfoQueryResultRequested> {
  private final CloudwatchComponent cloudwatchComponent;

  @Override
  public void accept(GetBillingInfoQueryResultRequested event) {
    GetQueryResultsResponse getQueryResultsResponse =
        cloudwatchComponent.getQueryResult(event.getQueryId());
    QueryStatus status = getQueryResultsResponse.status();

    if (RUNNING.equals(status)) {
      // fail on purpose so event backs off
      throw new RuntimeException("query is still running");
    }
    if (COMPLETE.equals(status)) {
      List<List<ResultField>> results = getQueryResultsResponse.results();
      List<ResultField> first = results.getFirst();
      assert first.size() == 2;
      var totalDurationMinutes = first.getFirst();
      assert "totalDurationMinutes".equals(totalDurationMinutes.field());
      var totalMemoryMb = first.getLast();
      assert "totalMemoryMB".equals(totalMemoryMb.field());

      // compute price and update billing info with queryId = something
    } else if (FAILED.equals(status)) {
      // what do we do on query fail?
    }
  }
}
