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

  public List<PaymentMethod> getPaymentMethod(String cId) throws StripeException {
    Customer customer = Customer.retrieve(cId);

    PaymentMethodCollection paymentMethods = customer.listPaymentMethods();

    return paymentMethods.getData();
  }

  public Customer createCustomer(PaymentCustomerBase customer, String userId) throws StripeException {
    CustomerCreateParams params =
        CustomerCreateParams.builder().setEmail(customer.getEmail()).setName(customer.getName()).build();
    Customer newCustomer = Customer.create(params, getRequestOption());
    User currentUser = userService.getUserById(userId);
    currentUser.setStripeCustomerId(newCustomer.getId());
    userService.updateUser(currentUser);
    return newCustomer;
  }

  public PaymentMethod attach(String cId, String pmId) throws StripeException {
    PaymentMethod resource = PaymentMethod.retrieve(pmId);

    PaymentMethodAttachParams params = PaymentMethodAttachParams.builder().setCustomer(cId).build();

    return resource.attach(params);
  }

  public PaymentMethod detach(String cId, String pmId) throws StripeException {
    PaymentMethod resource = PaymentMethod.retrieve("pm_1MqLiJLkdIwHu7ixUEgbFdYF");
    PaymentMethodDetachParams params = PaymentMethodDetachParams.builder().build();
    return resource.detach(params);
  }

  public Customer setDefault(String cId, String pmId) throws StripeException {
    Customer resource = Customer.retrieve(cId);

    CustomerUpdateParams.InvoiceSettings invoiceSettings =
        CustomerUpdateParams.InvoiceSettings.builder().setDefaultPaymentMethod(pmId).build();
    CustomerUpdateParams params =
        CustomerUpdateParams.builder().setInvoiceSettings(invoiceSettings).build();

    return resource.update(params);
  }

  RequestOptions getRequestOption() {
    return RequestOptions.builder().setApiKey(stripeConf.getSecretKey()).build();
  }
}
