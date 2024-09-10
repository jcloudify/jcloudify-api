package api.jcloudify.app.service;

import api.jcloudify.app.repository.jpa.UserPaymentRequestRepository;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserPaymentRequestService {
  private final UserPaymentRequestRepository repository;

  public UserPaymentRequest save(UserPaymentRequest userPaymentRequest) {
    return repository.save(userPaymentRequest);
  }
}
