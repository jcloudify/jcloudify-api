package api.jcloudify.app.service;

import com.stripe.Stripe;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class StripeConf {
  private String apiKey;

  public StripeConf(@Value("${stripe.api.key}") String apiKey) {
    this.apiKey = apiKey;
    Stripe.apiKey = apiKey;
  }
}
