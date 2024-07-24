package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.PaymentMapper;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodResponse;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodsAction;
import api.jcloudify.app.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper mapper;

    @GetMapping("/users/{userId}/payment-method")
    public PaymentMethodResponse getPaymentMethods(@PathVariable String userId) {
        List<PaymentMethod> data = paymentService.getPaymentMethods(userId).stream().map(mapper::toRest).toList();
        return new PaymentMethodResponse().data(data);
    }

    @PutMapping("/users/{userId}/payment-method")
    public PaymentMethodResponse managePaymentMethod(@PathVariable String userId, @RequestBody PaymentMethodsAction paymentMethodsAction) {
        List<PaymentMethod> data = paymentService.getPaymentMethods(userId).stream().map(mapper::toRest).toList();
        return new PaymentMethodResponse().data(data);
    }
}
