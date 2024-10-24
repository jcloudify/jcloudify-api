package api.jcloudify.app.endpoint.event;

import static api.jcloudify.app.endpoint.event.utils.TestMocks.computeStackCreated;
import static api.jcloudify.app.endpoint.event.utils.TestMocks.newSavedComputeStack;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdateTriggered;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.StackService;
import api.jcloudify.app.service.event.ComputeStackCrupdateTriggeredService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ComputeStackCrupdateTriggeredServiceIT extends MockedThirdParties {
  @Autowired StackService service;
  @MockBean ExtendedBucketComponent extendedBucketComponent;
  @MockBean EventProducer<ComputeStackCrupdateTriggered> eventProducer;
  @Autowired ComputeStackCrupdateTriggeredService subject;

  private List<Stack> ignoreIdsAndDatetime(List<Stack> stacks) {
    return stacks.stream()
        .peek(
            stack -> {
              stack.setId(null);
              stack.setCreationDatetime(null);
              stack.setUpdateDatetime(null);
            })
        .toList();
  }

  @Test
  void crupdated_compute_stack_info_is_saved() {
    when(cloudformationComponent.getStackIdByName("poja_app_compute_stack")).thenReturn("1234");
    subject.accept(computeStackCreated());

    var persistedStacks = service.findAllByEnvId(POJA_APPLICATION_ENVIRONMENT_ID);

    assertTrue(ignoreIdsAndDatetime(persistedStacks).contains(newSavedComputeStack()));
  }
}
