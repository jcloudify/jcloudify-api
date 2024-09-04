package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.UserMonthlyPaymentRequested;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.StripeService;
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

  @Override
  public void accept(UserMonthlyPaymentRequested userMonthlyPaymentRequested) {
    processInvoice(
        userMonthlyPaymentRequested.getUserId(), userMonthlyPaymentRequested.getCustomerId());
  }

  private void processInvoice(String userId, String customerId) {
    List<Application> app = applicationService.findAllByUserId(userId);
    Invoice invoice = stripeService.createInvoice(customerId);
    app.forEach(
        item -> {
          stripeService.createInvoiceItem(invoice.getId(), item.getPrice(), item.getName());
        });
    stripeService.finalizeInvoice(invoice.getId());
    stripeService.payInvoice(invoice.getId());

    // Save process on db
  }
}
