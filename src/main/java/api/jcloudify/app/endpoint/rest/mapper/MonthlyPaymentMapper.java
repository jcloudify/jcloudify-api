package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Payment;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MonthlyPaymentMapper {

  public Payment toRest(UserPaymentRequest domain) {
    return new Payment()
        .id(domain.getId())
        .amount(Math.toIntExact(domain.getAmount()))
        .invoiceId(domain.getInvoiceId())
        .invoiceStatus(Payment.InvoiceStatusEnum.valueOf(domain.getInvoiceStatus().name()))
        .invoiceUrl(domain.getInvoiceUrl());
  }

  public List<Payment> toRest(List<UserPaymentRequest> monthlyPaymentRequests) {
    return monthlyPaymentRequests.stream().map(this::toRest).toList();
  }
}
