package api.jcloudify.app.service.pricing.calculator;

import api.jcloudify.app.service.pricing.PricingMethod;
import java.math.BigDecimal;

public interface PricingCalculator {
  boolean supports(PricingMethod pricingMethod);

  /**
   * @param method pricing method which will be applied to totalMemoryDurationMinutes
   * @param totalMemoryDurationMinutes result of the sum of duration in minutes * memory in MB. if
   *     a(mem1, dur1), b(mem1, dur2), c(mem3, dur3) then totalMemoryDurationMinutes is ((mem1 *
   *     (dur1 + dur2)) + (mem3 * dur3)) which is the sum
   * @return pricing according to the method and the totalMemoryDurationMinutes
   */
  BigDecimal computePrice(PricingMethod method, BigDecimal totalMemoryDurationMinutes);
}
