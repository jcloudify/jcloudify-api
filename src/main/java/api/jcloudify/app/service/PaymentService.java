package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.PaymentCustomerBase;
import api.jcloudify.app.repository.model.User;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodDetachParams;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {
  private final StripeConf stripeConf;
  private final UserService userService;

  public List<PaymentMethod> getPaymentMethods(String userId) throws StripeException {
    User user = userService.getUserById(userId);
    Customer customer = Customer.retrieve(user.getStripeCustomerId());

    PaymentMethodCollection paymentMethods = customer.listPaymentMethods();

    return paymentMethods.getData();
  }

  public Customer createCustomer(PaymentCustomerBase customer, String userId)
      throws StripeException {
    CustomerCreateParams params =
        CustomerCreateParams.builder()
            .setEmail(customer.getEmail())
            .setName(customer.getName())
            .build();
    Customer newCustomer = Customer.create(params, getRequestOption());
    User currentUser = userService.getUserById(userId);
    currentUser.setStripeCustomerId(newCustomer.getId());
    userService.updateUser(currentUser);
    return newCustomer;
  }

  public List<PaymentMethod> attach(String paymentMethodId, String userId) throws StripeException {
    User user = userService.getUserById(userId);
    PaymentMethod resource = PaymentMethod.retrieve(paymentMethodId);

    PaymentMethodAttachParams params =
        PaymentMethodAttachParams.builder().setCustomer(user.getStripeCustomerId()).build();
    resource.attach(params);
    return this.getPaymentMethods(userId);
  }

  public List<PaymentMethod> detach(String paymentMethodId, String userId) throws StripeException {
    PaymentMethod resource = PaymentMethod.retrieve(paymentMethodId);
    PaymentMethodDetachParams params = PaymentMethodDetachParams.builder().build();
    resource.detach(params);
    return this.getPaymentMethods(userId);
  }

  public Customer setDefault(String paymentMethodId, String userId) throws StripeException {
    User user = userService.getUserById(userId);
    Customer resource = Customer.retrieve(user.getStripeCustomerId());

    CustomerUpdateParams.InvoiceSettings invoiceSettings =
        CustomerUpdateParams.InvoiceSettings.builder()
            .setDefaultPaymentMethod(paymentMethodId)
            .build();
    CustomerUpdateParams params =
        CustomerUpdateParams.builder().setInvoiceSettings(invoiceSettings).build();

    return resource.update(params);
  }

  RequestOptions getRequestOption() {
    return RequestOptions.builder().setApiKey(stripeConf.getSecretKey()).build();
  }
}
