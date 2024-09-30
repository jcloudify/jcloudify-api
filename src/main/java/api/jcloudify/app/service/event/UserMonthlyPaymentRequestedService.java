package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.UserMonthlyPaymentRequested;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.BillingInfo;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import api.jcloudify.app.repository.model.enums.InvoiceStatus;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.BillingInfoService;
import api.jcloudify.app.service.StripeService;
import api.jcloudify.app.service.UserPaymentRequestService;
import com.stripe.model.Invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

import com.stripe.model.InvoiceItem;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMonthlyPaymentRequestedService implements Consumer<UserMonthlyPaymentRequested> {
  private final ApplicationService applicationService;
  private final BillingInfoService billingInfoService;
  private final StripeService stripeService;
  private final UserPaymentRequestService userPaymentRequestService;

  @Override
  public void accept(UserMonthlyPaymentRequested userMonthlyPaymentRequested) {
    Invoice invoice =
        createAndPayInvoice(
            userMonthlyPaymentRequested.getUserId(), userMonthlyPaymentRequested.getCustomerId());
    String paymentStatus = stripeService.getPaymentStatus(invoice);
    var paymentRequest =
        userPaymentRequestService.save(
            UserPaymentRequest.builder()
                .paymentRequestId(userMonthlyPaymentRequested.getParentId())
                .amount(invoice.getAmountDue())
                .invoiceId(invoice.getId())
                .invoiceUrl(invoice.getInvoicePdf())
                .invoiceStatus(InvoiceStatus.valueOf(paymentStatus))
                .userId(userMonthlyPaymentRequested.getUserId())
                .build());
  }

  private Invoice createAndPayInvoice(String userId, String customerId) {

    List<Application> app = applicationService.findAllByUserId(userId);
    Instant currentDate = Instant.now();
    Invoice invoice = stripeService.createInvoice(customerId);
    app.forEach(
        item -> {
          createInvoiceItem(userId, invoice.getId(), item, currentDate.minus(1, ChronoUnit.MONTHS), currentDate);
        });
    stripeService.finalizeInvoice(invoice.getId());
    return stripeService.payInvoice(invoice.getId());
  }

  private InvoiceItem createInvoiceItem(String userId, String invoiceId, Application app, Instant startTime, Instant endTime) {

    var amountToDue = billingInfoService
        .getUserBillingInfoByApplication(userId, app.getId(), startTime, endTime)
        .stream()
        .map(BillingInfo::getComputedPriceInUsd)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return stripeService.createInvoiceItem(invoiceId, amountToDue.multiply(new BigDecimal(100)).longValue(), app.getName());
  }
}
