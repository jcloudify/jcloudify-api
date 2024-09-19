package api.jcloudify.app.service;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.UserPaymentRequestRepository;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserPaymentRequestService {
  private final UserPaymentRequestRepository repository;

  public UserPaymentRequest save(UserPaymentRequest userPaymentRequest) {
    return repository.save(userPaymentRequest);
  }

  public UserPaymentRequest getById(String id) {
    return findById(id)
        .orElseThrow(
            () -> new NotFoundException("Payment identified by id=" + id + " not found"));
  }

  public Optional<UserPaymentRequest> findById(String id) {
    return repository.findById(id);
  }
}
