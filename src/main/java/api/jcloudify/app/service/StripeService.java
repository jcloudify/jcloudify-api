package api.jcloudify.app.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

  public List<PaymentMethod> getPaymentMethods(String customerId) {
      try {
          Customer customer = Customer.retrieve(customerId);

          PaymentMethodCollection pmCollection = customer.listPaymentMethods();
          return pmCollection.getData();
      } catch (StripeException e) {
          throw new RuntimeException(e);
      }
  }

  public Customer setDefaultPaymentMethod(String customerId, String paymentMethodId) {
      try {
          Customer customer = Customer.retrieve(customerId);

          CustomerUpdateParams.InvoiceSettings invoiveSettingParams =
                  CustomerUpdateParams.InvoiceSettings.builder()
                          .setDefaultPaymentMethod(paymentMethodId)
                          .build();
          CustomerUpdateParams params = CustomerUpdateParams.builder()
                  .setInvoiceSettings(invoiveSettingParams)
                  .build();
          return customer.update(params);
      } catch (StripeException e) {
          throw new RuntimeException(e);
      }
  }

    public PaymentMethod attachPaymentMethod(String customerId, String paymentMethodId) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build();
            return paymentMethod.attach(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public PaymentMethod detachPaymentMethod(String paymentMethodId) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                    .build();
            return paymentMethod.attach(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

  private RequestOptions getRequestOption() {
    return RequestOptions.builder().setApiKey(stripeConf.getApiKey()).build();
  }
}
