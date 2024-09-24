package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdated;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.repository.jpa.dao.StackDao;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.StackService;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ComputeStackCrupdatedService implements Consumer<ComputeStackCrupdated> {
  private final StackService stackService;
  private final StackDao stackDao;
  private final EventProducer<StackCrupdated> eventProducer;

  @Override
  public void accept(ComputeStackCrupdated computeStackCrupdated) {
    String userId = computeStackCrupdated.getUserId();
    String applicationId = computeStackCrupdated.getAppId();
    String environmentId = computeStackCrupdated.getEnvId();
    String stackName = computeStackCrupdated.getStackName();
    Optional<String> cfStackId = stackService.getCloudformationStackId(stackName);

    Optional<Stack> stack = stackDao.findByCriteria(applicationId, environmentId, COMPUTE);
    if (cfStackId.isEmpty()) {
      log.info("Stack({}) does not exist", stackName);
      throw new InternalServerErrorException(String.format("Stack(%s) doesn't exists", stackName));
    } else {
      Stack saved;
      if (stack.isPresent()) {
        Stack toUpdate = stack.get();
        toUpdate.toBuilder().cfStackId(cfStackId.get()).build();
        saved = stackService.save(toUpdate);
      } else {
        saved =
            stackService.save(
                Stack.builder()
                    .name(stackName)
                    .cfStackId(cfStackId.get())
                    .applicationId(applicationId)
                    .environmentId(environmentId)
                    .type(COMPUTE)
                    .build());
      }
      eventProducer.accept(List.of(StackCrupdated.builder().userId(userId).stack(saved).build()));
    }
  }
}
