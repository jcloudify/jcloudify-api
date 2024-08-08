package api.jcloudify.app.service;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.model.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodDetachParams;
import java.util.List;
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
      return Customer.create(params, getRequestOption());
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public List<PaymentMethod> getPaymentMethods(String customerId) {
    try {
      Customer customer = Customer.retrieve(customerId);
      PaymentMethodCollection pmCollection = customer.listPaymentMethods();
      return pmCollection.getData();
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public PaymentMethod setDefaultPaymentMethod(String customerId, String paymentMethodId) {
    try {
      Customer customer = Customer.retrieve(customerId);
      CustomerUpdateParams.InvoiceSettings invoiveSettingParams =
          CustomerUpdateParams.InvoiceSettings.builder()
              .setDefaultPaymentMethod(paymentMethodId)
              .build();
      CustomerUpdateParams params =
          CustomerUpdateParams.builder().setInvoiceSettings(invoiveSettingParams).build();
      customer.update(params);
      return PaymentMethod.retrieve(paymentMethodId);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public PaymentMethod attachPaymentMethod(String customerId, String paymentMethodId) {
    try {
      PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

      PaymentMethodAttachParams params =
          PaymentMethodAttachParams.builder().setCustomer(customerId).build();
      return paymentMethod.attach(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public PaymentMethod detachPaymentMethod(String paymentMethodId) {
    try {
      PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

      PaymentMethodDetachParams params = PaymentMethodDetachParams.builder().build();
      return paymentMethod.detach(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public PaymentMethod retrievePaymentMethod(String paymentMethodId) {
    try {
      return PaymentMethod.retrieve(paymentMethodId);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  // Customer
  public Customer retrieveCustomer(String customerId) {
    try {
      return Customer.retrieve(customerId, getRequestOption());
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public Customer updateCustomer(String id, String name, String email, String phone) {
    try {
      Customer resource = Customer.retrieve(id, getRequestOption());
      CustomerUpdateParams params =
          CustomerUpdateParams.builder().setName(name).setEmail(email).setPhone(phone).build();
      return resource.update(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public PaymentIntent createPaymentIntent(Long amount, String returnUrl, String customerId) {
    try {
      Customer customer = Customer.retrieve(customerId);
      PaymentIntentCreateParams params =
          PaymentIntentCreateParams.builder()
              .setAmount(amount)
              .setCurrency("usd")
              .setCustomer(customer.getId())
              .setConfirm(true)
              .setReceiptEmail(customer.getEmail())
              .setPaymentMethod(customer.getInvoiceSettings().getDefaultPaymentMethod())
              .setReturnUrl(returnUrl)
              .build();

      return PaymentIntent.create(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  private RequestOptions getRequestOption() {
    return RequestOptions.builder().setApiKey(stripeConf.getApiKey()).build();
  }
}
