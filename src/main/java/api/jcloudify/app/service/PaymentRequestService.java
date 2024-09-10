package api.jcloudify.app.service;

import api.jcloudify.app.repository.jpa.PaymentRequestRepository;
import api.jcloudify.app.repository.model.PaymentRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentRequestService {
  private final PaymentRequestRepository repository;

  public PaymentRequest save(PaymentRequest paymentRequest) {
    return repository.save(paymentRequest);
  }
}
