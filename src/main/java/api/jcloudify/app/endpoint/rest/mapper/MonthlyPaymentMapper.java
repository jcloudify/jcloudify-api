package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Payment;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MonthlyPaymentMapper {

  public Payment toRest(UserPaymentRequest domain) {
    return new Payment()
        .id(domain.getId())
        .invoiceId(domain.getInvoiceId())
        .invoiceStatus(Payment.InvoiceStatusEnum.valueOf(domain.getInvoiceStatus().name()))
        .invoiceUrl(domain.getInvoiceUrl());
  }

  public List<Payment> toRest (List<UserPaymentRequest> monthlyPaymentRequests){
    return monthlyPaymentRequests.stream().map(this::toRest).toList();
  }

}
