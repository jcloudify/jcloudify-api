package api.jcloudify.app.service;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.UserMonthlyPaymentRequested;
import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.PaymentCustomer;
import api.jcloudify.app.endpoint.rest.model.PaymentMethodsAction;
import api.jcloudify.app.model.User;
import api.jcloudify.app.repository.model.PaymentRequest;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import api.jcloudify.app.repository.model.enums.InvoiceStatus;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentMethod;
import java.time.Instant;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {
  private final EventProducer<UserMonthlyPaymentRequested> eventProducer;
  private final StripeService stripeService;
  private final UserService userService;
  private final PaymentRequestService paymentRequestService;
  private final UserPaymentRequestService userPaymentRequestService;

  public String createCustomer(CreateUser createUser) {
    String name = createUser.getFirstName() + " " + createUser.getLastName();
    Customer customer = stripeService.createCustomer(name, createUser.getEmail());
    return customer.getId();
  }

  public PaymentMethod managePaymentMethod(String userId, PaymentMethodsAction pmAction) {
    User user = userService.getUserById(userId);
    String stripeId = user.getStripeId();
    String pmId = pmAction.getPaymentMethodId();
    PaymentMethodsAction.ActionEnum action = pmAction.getAction();
    return switch (action) {
      case ATTACH:
        PaymentMethod paymentMethod = stripeService.attachPaymentMethod(stripeId, pmId);
        if (TRUE.equals(pmAction.getSetDefault())) {
          stripeService.setDefaultPaymentMethod(stripeId, pmId);
        }
        yield paymentMethod;
      case DETACH:
        yield stripeService.detachPaymentMethod(pmId);
    };
  }

  public List<PaymentMethod> getPaymentMethods(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.getPaymentMethods(user.getStripeId());
  }

  public Customer getCustomer(String userId) {
    User user = userService.getUserById(userId);
    return stripeService.retrieveCustomer(user.getStripeId());
  }

  public Customer updateCustomer(PaymentCustomer customer) {
    return stripeService.updateCustomer(
        customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone());
  }

  public void initiatePaymentAttempts() {
    String parentId = randomUUID().toString();
    Month period = YearMonth.now().minusMonths(1).getMonth();
    paymentRequestService.save(
        PaymentRequest.builder().requestInstant(Instant.now()).id(parentId).period(period).build());
    List<User> users = userService.getAllUsers();
    var events =
        users.stream().map(user -> toUserMonthlyPayementRequested(user, parentId)).toList();
    eventProducer.accept(events);
  }

  private UserMonthlyPaymentRequested toUserMonthlyPayementRequested(User user, String parentId) {
    return UserMonthlyPaymentRequested.builder()
        .parentId(parentId)
        .userId(user.getId())
        .customerId(user.getStripeId())
        .build();
  }

  public UserPaymentRequest payInvoiceManually(
      String paymentId, String invoiceId, String paymentMethodId) {
    Invoice invoice = stripeService.payInvoice(invoiceId, paymentMethodId);
    String paymentStatus = stripeService.getPaymentStatus(invoice);
    var payment = userPaymentRequestService.getById(paymentId);
    payment.setInvoiceStatus(InvoiceStatus.valueOf(paymentStatus));
    return userPaymentRequestService.save(payment);
  }
}
