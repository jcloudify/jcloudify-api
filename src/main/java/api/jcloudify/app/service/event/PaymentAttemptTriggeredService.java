package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.PaymentAttemptTriggered;
import api.jcloudify.app.service.PaymentService;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentAttemptTriggeredService implements Consumer<PaymentAttemptTriggered> {
  private final PaymentService paymentService;

  @Override
  public void accept(PaymentAttemptTriggered paymentAttemptTriggered) {
    paymentService.paymentAttempts();
  }
}
