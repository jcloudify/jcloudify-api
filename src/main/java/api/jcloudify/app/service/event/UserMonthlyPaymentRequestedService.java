package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.UserMonthlyPaymentRequested;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.StripeService;
import api.jcloudify.app.service.UserPaymentRequestService;
import com.stripe.model.Invoice;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMonthlyPaymentRequestedService implements Consumer<UserMonthlyPaymentRequested> {
  private final ApplicationService applicationService;
  private final StripeService stripeService;
  private final UserPaymentRequestService userPaymentRequestService;

  @Override
  public void accept(UserMonthlyPaymentRequested userMonthlyPaymentRequested) {
    Invoice invoice =
        processInvoice(
            userMonthlyPaymentRequested.getUserId(), userMonthlyPaymentRequested.getCustomerId());
    var paymentRequest =
        userPaymentRequestService.save(
            UserPaymentRequest.builder()
                .parentId(userMonthlyPaymentRequested.getParentId())
                .invoiceId(invoice.getId())
                .invoiceUrl(invoice.getInvoicePdf())
                .status(invoice.getStatus())
                .userId(userMonthlyPaymentRequested.getUserId())
                .build());
  }

  private Invoice processInvoice(String userId, String customerId) {
    List<Application> app = applicationService.findAllByUserId(userId);
    Invoice invoice = stripeService.createInvoice(customerId);
    app.forEach(
        item -> {
          stripeService.createInvoiceItem(invoice.getId(), item.getPrice(), item.getName());
        });
    stripeService.finalizeInvoice(invoice.getId());
    return stripeService.payInvoice(invoice.getId());
  }
}
