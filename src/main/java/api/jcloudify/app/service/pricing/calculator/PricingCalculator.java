package api.jcloudify.app.service.pricing.calculator;

import api.jcloudify.app.service.pricing.PricingMethod;
import java.math.BigDecimal;
import java.time.Duration;

public interface PricingCalculator {
  boolean supports(PricingMethod pricingMethod);

  BigDecimal computePrice(PricingMethod method, Duration duration, BigDecimal memoryUsedInMo);
}
