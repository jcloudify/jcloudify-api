package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Payment;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
  public PaymentMethod toRest(com.stripe.model.PaymentMethod domain) {
    com.stripe.model.PaymentMethod.Card card = domain.getCard();
    return new PaymentMethod()
        .id(domain.getId())
        .type(domain.getType())
        .brand(card.getBrand())
        .last4(card.getLast4());
  }
}
