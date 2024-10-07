package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.UserPaymentRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPaymentRequestRepository extends JpaRepository<UserPaymentRequest, String> {
  List<UserPaymentRequest> findAllByUserId(String userId, Pageable pageable);
}
