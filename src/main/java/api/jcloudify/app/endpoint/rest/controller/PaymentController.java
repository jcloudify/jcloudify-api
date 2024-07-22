package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.PaymentMapper;
import api.jcloudify.app.endpoint.rest.model.CreatePaymentCustomerRequestBody;
import api.jcloudify.app.endpoint.rest.model.CreatePaymentCustomerResponse;
import api.jcloudify.app.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping("/users/{userId}/payment-methods/customers")
    public CreatePaymentCustomerResponse createCustomer(@PathVariable String userId, @RequestBody CreatePaymentCustomerRequestBody customer) throws StripeException {
        Customer created = paymentService.createCustomer(Objects.requireNonNull(customer.getData()), userId);
        return new CreatePaymentCustomerResponse().data(paymentMapper.restCustomer(created));
    }
}
