package api.jcloudify.app.service;

import static java.lang.Boolean.TRUE;

import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.PaymentCustomer;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodsAction;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.User;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentMethod;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {
  private final StripeService stripeService;
  private final UserService userService;
  private final ApplicationService applicationService;

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
    return switch (action) {
      case ATTACH:
        PaymentMethod paymentMethod = stripeService.attachPaymentMethod(stripeId, pmId);
        if (TRUE.equals(pmAction.getSetDefault())) {
          stripeService.setDefaultPaymentMethod(stripeId, pmId);
        }
        yield paymentMethod;
      case DETACH:
        yield stripeService.detachPaymentMethod(pmId);
    };
  }

  public List<PaymentMethod> getPaymentMethods(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.getPaymentMethods(user.getStripeId());
  }

  public Customer getCustomer(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.retrieveCustomer(user.getStripeId());
  }

  public Customer updateCustomer(PaymentCustomer customer) {
    return stripeService.updateCustomer(
        customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone());
  }

  public void makeAPayment() {
    List<User> users = userService.getAllUsers();
    users.forEach(
        user -> {
          invoiceCreationProcess(user.getId(), user.getStripeId());
        });
  }

  private void invoiceCreationProcess(String userId, String customerId) {
    List<Application> app = applicationService.findAllByUserId(userId);
    Invoice invoice = stripeService.createInvoice(customerId);
    app.forEach(
        item -> {
          if (item.getPrice() != 0) {
            stripeService.createInvoiceItem(invoice.getId(), item.getPrice(), item.getName());
          }
        });
    stripeService.finalizeInvoice(invoice.getId());
    stripeService.payInvoice(invoice.getId());
  }
}
