package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, String> {}
