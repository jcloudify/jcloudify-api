package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import com.stripe.model.Customer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {
  private final StripeService stripeService;

  public String createCustomer(CreateUser createUser) {
    String name = createUser.getFirstName() + " " + createUser.getLastName();
    Customer customer = stripeService.createCustomer(name, createUser.getEmail());
    return customer.getId();
  }
}
