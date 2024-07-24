package api.jcloudify.app.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StripeService {
  private final StripeConf stripeConf;

  public Customer createCustomer(String name, String email) {
    try {
      CustomerCreateParams params =
          CustomerCreateParams.builder().setName(name).setEmail(email).build();
      return Customer.create(params);
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }

  private RequestOptions getRequestOption() {
    return RequestOptions.builder().setApiKey(stripeConf.getApiKey()).build();
  }
}
