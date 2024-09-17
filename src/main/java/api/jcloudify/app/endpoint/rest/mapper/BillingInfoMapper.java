package api.jcloudify.app.endpoint.rest.mapper;

import static api.jcloudify.app.endpoint.rest.model.DurationUnit.MINUTES;

import api.jcloudify.app.endpoint.rest.model.BillingInfo;
import api.jcloudify.app.endpoint.rest.model.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class BillingInfoMapper {
  public BillingInfo toRest(
      api.jcloudify.app.repository.model.BillingInfo domain, Instant startTime, Instant endTime) {
    var duration = new Duration().amount(domain.getComputedDurationInMinutes()).unit(MINUTES);
    return new BillingInfo()
        .startTime(startTime)
        .endTime(endTime)
        .computedPrice(domain.getComputedPriceInUsd())
        .pricingMethod(domain.getPricingMethod().getName())
        .computeTime(domain.getComputeDatetime())
        .resourceInvocationTotalDuration(duration);
  }
}
