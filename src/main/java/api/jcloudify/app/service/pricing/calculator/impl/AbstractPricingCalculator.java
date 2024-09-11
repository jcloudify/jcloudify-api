package api.jcloudify.app.service.pricing.calculator.impl;

import static lombok.AccessLevel.PROTECTED;

import api.jcloudify.app.service.pricing.PricingMethod;
import api.jcloudify.app.service.pricing.calculator.PricingCalculator;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractPricingCalculator implements PricingCalculator {
  @Getter(PROTECTED)
  private final PricingMethod supportedMethod;

  protected AbstractPricingCalculator(PricingMethod supportedMethod) {
    this.supportedMethod = supportedMethod;
  }

  private void checkPricingMethod(PricingMethod pricingMethod) {
    if (!supports(pricingMethod)) {
      throw new UnsupportedOperationException(
          "Pricing method not supported by " + this.getClass().getName());
    }
  }

  @Override
  public final boolean supports(PricingMethod pricingMethod) {
    return this.supportedMethod.equals(pricingMethod);
  }

  @Override
  public final BigDecimal computePrice(
      PricingMethod method, BigDecimal totalMemoryDurationMinutes) {
    checkPricingMethod(method);
    return computePrice(totalMemoryDurationMinutes);
  }

  public abstract BigDecimal computePrice(BigDecimal totalMemoryDurationMinutes);
}
