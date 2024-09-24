package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.UserPaymentRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPaymentRequestRepository extends JpaRepository<UserPaymentRequest, String> {
  List<UserPaymentRequest> findAllByUserId(String userId, Pageable pageable);
}
