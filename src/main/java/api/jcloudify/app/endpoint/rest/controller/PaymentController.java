package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.PaymentCustomerMapper;
import api.jcloudify.app.endpoint.rest.mapper.PaymentMapper;
import api.jcloudify.app.endpoint.rest.model.PaymentCustomer;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodResponse;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodsAction;
import api.jcloudify.app.service.PaymentService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PaymentController {
  private final PaymentService paymentService;
  private final PaymentMapper mapper;
  private final PaymentCustomerMapper customerMapper;

  @GetMapping("/users/{userId}/payment-details/payment-methods")
  public PaymentMethodResponse getPaymentMethods(@PathVariable String userId) {
    List<PaymentMethod> data =
        paymentService.getPaymentMethods(userId).stream().map(mapper::toRest).toList();
    return new PaymentMethodResponse().data(data);
  }

  @PutMapping("/users/{userId}/payment-details/payment-methods")
  public PaymentMethodResponse managePaymentMethod(
      @PathVariable String userId, @RequestBody PaymentMethodsAction paymentMethodsAction) {
    paymentService.managePaymentMethod(userId, paymentMethodsAction);
    List<PaymentMethod> data =
        paymentService.getPaymentMethods(userId).stream().map(mapper::toRest).toList();
    return new PaymentMethodResponse().data(data);
  }

  @GetMapping("/users/{userId}/payment-details")
  public PaymentCustomer getPaymentCustomer(@PathVariable String userId) {
    return customerMapper.toRest(paymentService.getCustomer(userId));
  }

  @PutMapping("/users/{userId}/payment-details")
  public PaymentCustomer updatePaymentCustomer(
      @PathVariable String userId, @RequestBody PaymentCustomer customer) {
    return customerMapper.toRest(paymentService.updateCustomer(customer));
  }
}
