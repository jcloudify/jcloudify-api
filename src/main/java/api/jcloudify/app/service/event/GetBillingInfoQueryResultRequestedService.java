package api.jcloudify.app.service.event;

import static api.jcloudify.app.service.pricing.PricingMethod.TEN_MICRO;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.COMPLETE;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.FAILED;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.RUNNING;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.model.GetBillingInfoQueryResultRequested;
import api.jcloudify.app.service.pricing.calculator.PricingCalculator;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResultField;

@Component
@AllArgsConstructor
@Slf4j
public class GetBillingInfoQueryResultRequestedService
    implements Consumer<GetBillingInfoQueryResultRequested> {
  private final CloudwatchComponent cloudwatchComponent;
  private final PricingCalculator pricingCalculator;

  @Override
  public void accept(GetBillingInfoQueryResultRequested event) {
    GetQueryResultsResponse getQueryResultsResponse =
        cloudwatchComponent.getQueryResult(event.getQueryId());
    QueryStatus status = getQueryResultsResponse.status();
    // TODO: get billing info with queryId = event.getQueryId and take its pricingMethod
    var pricingMethod = TEN_MICRO;

    if (RUNNING.equals(status)) {
      // fail on purpose so event backs off
      throw new RuntimeException("query is still running");
    }
    if (COMPLETE.equals(status)) {
      log.info("query with ID {} completed successfully", event.getQueryId());
      List<List<ResultField>> results = getQueryResultsResponse.results();
      List<ResultField> first = results.getFirst();
      assert first.size() == 2;
      var totalMemoryDuration = first.getFirst();
      assert "totalMemoryDuration".equals(totalMemoryDuration.field());
      var computedPrice =
          pricingCalculator.computePrice(
              pricingMethod, new BigDecimal(totalMemoryDuration.value()));
      // TODO: update billing info with queryId = event.getQueryId
    } else if (FAILED.equals(status)) {
      log.info("query with ID {} failed, please inspect cloudwatch", event.getQueryId());
      // what do we do on query fail?
    }
  }
}
