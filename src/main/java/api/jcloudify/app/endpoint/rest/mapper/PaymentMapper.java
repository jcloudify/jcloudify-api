package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.PaymentCustomer;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import com.stripe.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentCustomer restCustomer(Customer domain) {
        return new PaymentCustomer()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .defaultPaymentMethod(domain.getInvoiceSettings().getDefaultPaymentMethod());
    }

    public PaymentMethod toRest(com.stripe.model.PaymentMethod domain) {
        return new PaymentMethod()
                .id(domain.getId())
                .type(PaymentMethod.TypeEnum.valueOf(domain.getType()))
                .brand(domain.getCard().getBrand())
                .last4(domain.getCard().getLast4());

    }
}
