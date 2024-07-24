package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodsAction;
import api.jcloudify.app.repository.model.User;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {
  private final StripeService stripeService;
  private final UserService userService;

  public String createCustomer(CreateUser createUser) {
    String name = createUser.getFirstName() + " " + createUser.getLastName();
    Customer customer = stripeService.createCustomer(name, createUser.getEmail());
    return customer.getId();
  }

  public void managePaymentMethod(String userId, PaymentMethodsAction pmAction) {

  }

  public List<PaymentMethod> getPaymentMethods(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.getPaymentMethods(user.getStripeId());
  }
}
