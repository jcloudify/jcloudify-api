package api.jcloudify.app.service;

import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.UserPaymentRequestRepository;
import api.jcloudify.app.repository.model.UserPaymentRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserPaymentRequestService {
  private final UserPaymentRequestRepository repository;

  public UserPaymentRequest save(UserPaymentRequest userPaymentRequest) {
    return repository.save(userPaymentRequest);
  }

  public Page<UserPaymentRequest> getUsersMonthlyPayments(String userId, PageFromOne pageFromOne, BoundedPageSize boundedPageSize){
    Pageable pageable = PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue());
    var data = repository.findAllByUserId(userId, pageable);
    return new Page<>(pageFromOne, boundedPageSize, data);
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
