package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Payment;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

  public Payment paymentToRest(UserPaymentRequest domain) {
    return new Payment()
        .id(domain.getId())
        .invoiceId(domain.getInvoiceId())
        .invoiceStatus(Payment.InvoiceStatusEnum.valueOf(domain.getInvoiceStatus().name()))
        .invoiceUrl(domain.getInvoiceUrl());
  }
  public PaymentMethod toRest(com.stripe.model.PaymentMethod domain) {
    com.stripe.model.PaymentMethod.Card card = domain.getCard();
    return new PaymentMethod()
        .id(domain.getId())
        .type(domain.getType())
        .brand(card.getBrand())
        .last4(card.getLast4());
  }
}
