package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.PaymentCustomer;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import api.jcloudify.app.service.StripeService;
import com.stripe.model.Customer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentCustomerMapper {
  private final StripeService stripeService;
  private final PaymentMapper paymentMapper;

  public PaymentCustomer toRest(Customer domain) {
    var invoiceSettings = domain.getInvoiceSettings();
    return new PaymentCustomer()
        .id(domain.getId())
        .name(domain.getName())
        .email(domain.getEmail())
        .phone(domain.getPhone())
        .defaultPaymentMethod(getDefaultPaymentMethod(invoiceSettings.getDefaultPaymentMethod()));
  }

  private PaymentMethod getDefaultPaymentMethod(String paymentMethodId) {
    return paymentMapper.toRest(stripeService.retrievePaymentMethod(paymentMethodId));
  }
}
