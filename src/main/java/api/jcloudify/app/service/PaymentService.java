package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodsAction;
import api.jcloudify.app.repository.model.User;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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

  public PaymentMethod managePaymentMethod(String userId, PaymentMethodsAction pmAction) {
    User user = userService.getUserById(userId);
    String stripeId = user.getStripeId();
    String pmId = pmAction.getPaymentMethodId();
    PaymentMethodsAction.ActionEnum action = pmAction.getAction();
    switch (action) {
      case ATTACH:
        PaymentMethod paymentMethod = stripeService.attachPaymentMethod(stripeId, pmId);
        if (pmAction.getSetDefault()) {
          stripeService.setDefaultPaymentMethod(stripeId, pmId);
        }
        return paymentMethod;
      case DETACH:
        return stripeService.detachPaymentMethod(pmId);
      default:
        return stripeService.setDefaultPaymentMethod(stripeId, pmId);
    }
  }

  public void initiatePayment(Long amount, String returnUrl, String customerId) {
    stripeService.createPaymentIntent(amount, returnUrl, customerId);
  }

  public List<PaymentMethod> getPaymentMethods(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.getPaymentMethods(user.getStripeId());
  }
}
