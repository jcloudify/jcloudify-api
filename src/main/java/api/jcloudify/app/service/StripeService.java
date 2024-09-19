package api.jcloudify.app.service;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.model.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceItem;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceFinalizeInvoiceParams;
import com.stripe.param.InvoiceItemCreateParams;
import com.stripe.param.InvoicePayParams;
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
      CustomerUpdateParams.InvoiceSettings invoiceSettingParams =
          CustomerUpdateParams.InvoiceSettings.builder()
              .setDefaultPaymentMethod(paymentMethodId)
              .build();
      CustomerUpdateParams params =
          CustomerUpdateParams.builder().setInvoiceSettings(invoiceSettingParams).build();
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
      return Customer.retrieve(customerId);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public Customer updateCustomer(String id, String name, String email, String phone) {
    try {
      Customer resource = Customer.retrieve(id);
      CustomerUpdateParams params =
          CustomerUpdateParams.builder().setName(name).setEmail(email).setPhone(phone).build();
      return resource.update(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public Invoice createInvoice(String customerId) {
    try {
      var params = InvoiceCreateParams.builder().setCustomer(customerId).setCurrency("usd").build();
      return Invoice.create(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public InvoiceItem createInvoiceItem(String invoiceId, Long amount, String appName) {
    try {
      InvoiceItemCreateParams params =
          InvoiceItemCreateParams.builder()
              .setInvoice(invoiceId)
              .setAmount(amount)
              .setDescription(appName)
              .build();
      return InvoiceItem.create(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public Invoice finalizeInvoice(String invoiceId) {
    try {
      Invoice resource = Invoice.retrieve(invoiceId);
      var params = InvoiceFinalizeInvoiceParams.builder().build();
      return resource.finalizeInvoice(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public Invoice payInvoice(String invoiceId) {
    try {
      Invoice resource = Invoice.retrieve(invoiceId);
      var params = InvoicePayParams.builder().build();
      return resource.pay(params);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public PaymentIntent retrievePaymentIntent(String paymentIntentId) {
    try {
      return PaymentIntent.retrieve(paymentIntentId);
    } catch (StripeException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  private RequestOptions getRequestOption() {
    return RequestOptions.builder().setApiKey(stripeConf.getApiKey()).build();
  }
}
