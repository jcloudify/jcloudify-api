package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.PaymentCustomer;
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
}
