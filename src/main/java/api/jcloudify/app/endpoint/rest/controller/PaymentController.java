package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.PaymentMapper;
import api.jcloudify.app.endpoint.rest.model.CreatePaymentCustomerRequestBody;
import api.jcloudify.app.endpoint.rest.model.CreatePaymentCustomerResponse;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodResponse;
import api.jcloudify.app.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    @GetMapping("/users/{userId}/payment-methods")
    public PaymentMethodResponse getPaymentMethods(@PathVariable String userId) throws StripeException {
        List<PaymentMethod> data = paymentService.getPaymentMethods(userId).stream().map(paymentMapper::toRest).toList();
        return new PaymentMethodResponse().data(data);
    }

    @PutMapping("/users/{userId}/payment-methods/{paymentMethodId}/attach")
    public PaymentMethodResponse attachPaymentMethod(@PathVariable String paymentMethodId, @PathVariable String userId) throws StripeException {
        List<PaymentMethod> data = paymentService.attach(paymentMethodId, userId).stream().map(paymentMapper::toRest).toList();

        return new PaymentMethodResponse().data(data);
    }

    @PutMapping("/users/{userId}/payment-methods/{paymentMethodId}/detach")
    public PaymentMethodResponse detachPaymentMethod(@PathVariable String paymentMethodId, @PathVariable String userId) throws StripeException {
        List<PaymentMethod> data = paymentService.detach(paymentMethodId, userId).stream().map(paymentMapper::toRest).toList();

        return new PaymentMethodResponse().data(data);
    }
}
