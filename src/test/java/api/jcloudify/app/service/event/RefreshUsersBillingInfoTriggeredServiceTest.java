package api.jcloudify.app.service.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.RefreshUserBillingInfoRequested;
import api.jcloudify.app.endpoint.event.model.RefreshUsersBillingInfoTriggered;
import api.jcloudify.app.repository.jpa.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class RefreshUsersBillingInfoTriggeredServiceTest extends MockedThirdParties {
  @Autowired private RefreshUsersBillingInfoTriggeredService subject;
  @MockBean private EventProducer<RefreshUserBillingInfoRequested> eventProducerMock;
  @MockBean private UserRepository userRepository;

  @Test
  void accept() {
    subject.accept(new RefreshUsersBillingInfoTriggered());

    verify(userRepository, times(1)).findAll();
    verify(eventProducerMock, times(1)).accept(anyCollection());
  }
}
