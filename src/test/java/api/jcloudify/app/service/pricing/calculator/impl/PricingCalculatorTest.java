package api.jcloudify.app.service.pricing.calculator.impl;

import static api.jcloudify.app.service.pricing.PricingMethod.TEN_MICRO;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.service.pricing.calculator.PricingCalculator;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PricingCalculatorTest extends MockedThirdParties {
  @Autowired PricingCalculator subject;

  @Test
  void computePrice() {
    var computedPrice =
        subject.computePrice(TEN_MICRO, Duration.ofSeconds(10_000), BigDecimal.valueOf(2048));

    assertEquals(new BigDecimal("3.39968"), computedPrice);
  }
}