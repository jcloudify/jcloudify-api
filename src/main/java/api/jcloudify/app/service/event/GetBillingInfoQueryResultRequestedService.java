package api.jcloudify.app.service.event;

import static api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus.FINISHED;
import static java.time.Instant.now;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.COMPLETE;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.FAILED;
import static software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus.RUNNING;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.model.GetBillingInfoQueryResultRequested;
import api.jcloudify.app.service.BillingInfoService;
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
  private final BillingInfoService billingInfoService;

  @Override
  public void accept(GetBillingInfoQueryResultRequested event) {
    GetQueryResultsResponse getQueryResultsResponse =
        cloudwatchComponent.getQueryResult(event.getQueryId());
    QueryStatus status = getQueryResultsResponse.status();
    var billingInfo = billingInfoService.getByQueryId(event.getQueryId());
    var pricingMethod = billingInfo.getPricingMethod();

    if (RUNNING.equals(status)) {
      // fail on purpose so event backs off
      throw new RuntimeException("query is still running");
    }
    if (COMPLETE.equals(status)) {
      log.info("query with ID {} completed successfully", event.getQueryId());
      List<List<ResultField>> results = getQueryResultsResponse.results();
      List<ResultField> first = results.getFirst();
      assert first.size() == 2;
      var billedMemoryDuration = first.getFirst();
      var computedBilledDuration = first.get(1);
      assert "billedMemoryDuration".equals(billedMemoryDuration.field());
      assert "computedBilledDuration".equals(computedBilledDuration.field());
      var computedPrice =
          pricingCalculator.computePrice(
              pricingMethod, new BigDecimal(billedMemoryDuration.value()));
      var computedDurationInMinutes = Double.valueOf(computedBilledDuration.value());
      billingInfoService.updateBillingInfoAfterCalculation(
          FINISHED, now(), computedDurationInMinutes, computedPrice, billingInfo.getId());
      log.info("Successfully completed calculation for billing info {}", billingInfo.getId());
    } else if (FAILED.equals(status)) {
      log.info("query with ID {} failed, please inspect cloudwatch", event.getQueryId());
      // what do we do on query fail?
    }
  }
}
