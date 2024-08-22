package api.jcloudify.app.service;

import static java.lang.Boolean.TRUE;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.PaymentCustomer;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodsAction;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import java.util.List;
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

  public PaymentMethod managePaymentMethod(String customerId, PaymentMethodsAction pmAction) {
    String pmId = pmAction.getPaymentMethodId();
    PaymentMethodsAction.ActionEnum action = pmAction.getAction();
    return switch (action) {
      case ATTACH:
        PaymentMethod paymentMethod = stripeService.attachPaymentMethod(customerId, pmId);
        if (TRUE.equals(pmAction.getSetDefault())) {
          stripeService.setDefaultPaymentMethod(customerId, pmId);
        }
        yield paymentMethod;
      case DETACH:
        yield stripeService.detachPaymentMethod(pmId);
    };
  }

  public List<PaymentMethod> getPaymentMethods(String customerId) {
    return stripeService.getPaymentMethods(customerId);
  }

  public Customer getCustomer(String customerId) {
    return stripeService.retrieveCustomer(customerId);
  }

  public Customer updateCustomer(PaymentCustomer customer) {
    return stripeService.updateCustomer(
        customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone());
  }
}
