package api.jcloudify.app.endpoint.rest.mapper;

import static api.jcloudify.app.endpoint.rest.model.DurationUnit.MINUTES;
import static api.jcloudify.app.endpoint.rest.model.MemoryUnit.MEGA_OCTET;

import api.jcloudify.app.endpoint.rest.model.BillingInfo;
import api.jcloudify.app.endpoint.rest.model.Duration;
import api.jcloudify.app.endpoint.rest.model.Memory;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class BillingInfoMapper {
  public BillingInfo toRest(
      api.jcloudify.app.repository.model.BillingInfo domain, Instant startTime, Instant endTime) {
    var duration = new Duration().amount(domain.getComputedDurationInMinutes()).unit(MINUTES);
    var memory = new Memory().amount(domain.getComputedMemoryUsedInMo()).unit(MEGA_OCTET);
    return new BillingInfo()
        .startTime(startTime)
        .endTime(endTime)
        .computedPrice(domain.getComputedPriceInUsd())
        .pricingMethod(domain.getPricingMethod())
        .computeTime(domain.getComputeDatetime())
        .resourceInvocationTotalDuration(duration)
        .resourceInvocationTotalMemoryUsed(memory);
  }
}
