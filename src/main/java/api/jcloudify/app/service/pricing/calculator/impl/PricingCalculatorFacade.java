package api.jcloudify.app.service.pricing.calculator.impl;

import api.jcloudify.app.service.pricing.PricingMethod;
import api.jcloudify.app.service.pricing.calculator.PricingCalculator;
import java.math.BigDecimal;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Slf4j
class PricingCalculatorFacade implements PricingCalculator {
  private final AbstractPricingCalculator tenMicroPricingCalculator;

  public PricingCalculatorFacade(
      @Qualifier("tenMicroPricingCalculator") AbstractPricingCalculator tenMicroPricingCalculator) {
    this.tenMicroPricingCalculator = tenMicroPricingCalculator;
  }

  private AbstractPricingCalculator getPricingCalculator(PricingMethod method) {
    log.info("supported method {}", tenMicroPricingCalculator.getSupportedMethod());
    if (tenMicroPricingCalculator.supports(method)) {
      return tenMicroPricingCalculator;
    }
    throw new IllegalArgumentException("unsupported pricing method: " + method);
  }

  @Override
  public boolean supports(PricingMethod pricingMethod) {
    throw new UnsupportedOperationException(
        "operation was not meant to be supported by this class");
  }

  @Override
  public BigDecimal computePrice(
      PricingMethod method, Duration duration, BigDecimal memoryUsedInMo) {
    return getPricingCalculator(method).computePrice(duration, memoryUsedInMo);
  }
}
