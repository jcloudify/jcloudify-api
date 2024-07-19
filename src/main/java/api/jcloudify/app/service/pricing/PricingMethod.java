package api.jcloudify.app.service.pricing;

import lombok.Getter;

@Getter
public enum PricingMethod {
  TEN_MICRO("10µ");
  private final String name;

  PricingMethod(String name) {
    this.name = name;
  }
}
