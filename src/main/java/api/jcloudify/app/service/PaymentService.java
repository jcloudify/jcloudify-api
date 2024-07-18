package api.jcloudify.app.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.net.RequestOptions;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {
    private final StripeConf stripeConf;

    public List<PaymentMethod> getPaymentMethod(String cId) throws StripeException {
        Customer customer = Customer.retrieve(cId);

        PaymentMethodCollection paymentMethods = customer.listPaymentMethods();

        return paymentMethods.getData();
    }

    RequestOptions getRequestOption() {
        return RequestOptions.builder().setApiKey(stripeConf.getSecretKey()).build();
    }
}
