package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.UserPaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPaymentRequestRepository extends JpaRepository<UserPaymentRequest, String> {}
