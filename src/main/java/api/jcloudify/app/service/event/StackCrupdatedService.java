package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum.PENDING;
import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_FAILED;
import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_IN_PROGRESS;
import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_SUCCESS;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.CREATE_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UPDATE_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE;
import static api.jcloudify.app.service.StackService.STACK_EVENT_FILENAME;
import static api.jcloudify.app.service.StackService.STACK_OUTPUT_FILENAME;
import static api.jcloudify.app.service.StackService.getStackEventsBucketKey;
import static api.jcloudify.app.service.StackService.getStackOutputsBucketKey;
import static java.io.File.createTempFile;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdateCompleted;
import api.jcloudify.app.endpoint.event.model.PojaEvent;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus;
import api.jcloudify.app.endpoint.rest.mapper.StackMapper;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.endpoint.rest.model.StackOutput;
import api.jcloudify.app.endpoint.rest.model.StackResourceStatusType;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.mail.Email;
import api.jcloudify.app.mail.Mailer;
import api.jcloudify.app.model.User;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.StackService;
import api.jcloudify.app.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class StackCrupdatedService implements Consumer<StackCrupdated> {
  private final CloudformationComponent cloudformationComponent;
  private final ExtendedBucketComponent bucketComponent;
  private final StackMapper mapper;
  private final ObjectMapper om;
  private final EventProducer<PojaEvent> eventProducer;
  private final UserService userService;
  private final Mailer mailer;
  private final StackService stackService;

  @Override
  public void accept(StackCrupdated stackCrupdated) {
    log.info("StackCrupdated: {}", stackCrupdated);
    Stack stack = stackCrupdated.getStack();
    String userId = stackCrupdated.getUserId();
    String stackEventsBucketKey =
        getStackEventsBucketKey(
            userId,
            stack.getApplicationId(),
            stack.getEnvironmentId(),
            stack.getId(),
            STACK_EVENT_FILENAME);
    StackCrupdateStatus stackCrupdateStatus =
        crupdateStackEvent(stack.getName(), stackEventsBucketKey);
    log.info("Current status: {}", stackCrupdateStatus);
    switch (stackCrupdateStatus) {
      case CRUPDATE_IN_PROGRESS -> throw new RuntimeException("fail to trigger event backoff.");
      case CRUPDATE_SUCCESS -> {
        log.info("CRUPDATE_SUCCESS for {}", stackCrupdated);
        String stackOutputsBucketKey =
            getStackOutputsBucketKey(
                userId,
                stack.getApplicationId(),
                stack.getEnvironmentId(),
                stack.getId(),
                STACK_OUTPUT_FILENAME);
        if (!COMPUTE.equals(stack.getType())) {
          // TODO: could be better if we create a new service to verify if allStacks except compute
          // were deployed, then send ComputeDeployRequested
          eventProducer.accept(
              List.of(
                  stackCrupdated.getParentAppEnvDeployRequested().toBuilder()
                      .currentIndependentStacksState(PENDING)
                      .build()));
          crupdateOutputs(stack.getName(), stackOutputsBucketKey);
          triggerStackResourcesRetrieving(userId, stack);
          return;
        }
        crupdateOutputs(stack.getName(), stackOutputsBucketKey);
        triggerStackResourcesRetrieving(userId, stack);
      }
      case CRUPDATE_FAILED -> {
        try {
          sendStackCrupdateFailedMail(userId, stack);
        } catch (AddressException e) {
          throw new InternalServerErrorException(e);
        }
      }
    }
  }

  private void crupdateOutputs(String stackName, String bucketKey) {
    List<StackOutput> stackOutputs =
        cloudformationComponent.getStackOutputs(stackName).stream().map(mapper::toRest).toList();
    try {
      File stackOutputJsonFile;
      if (bucketComponent.doesExist(bucketKey)) {
        stackOutputJsonFile = bucketComponent.download(bucketKey);
        List<StackOutput> actual = om.readValue(stackOutputJsonFile, new TypeReference<>() {});
        stackOutputs = mergeStackOutputList(actual, stackOutputs);
      } else {
        stackOutputJsonFile = createTempFile("output", ".json");
      }
      om.writeValue(stackOutputJsonFile, stackOutputs);
      bucketComponent.upload(stackOutputJsonFile, bucketKey);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  private StackCrupdateStatus crupdateStackEvent(String stackName, String bucketKey) {
    List<StackEvent> stackEvents = stackService.crupdateStackEvents(stackName, bucketKey);
    StackEvent latestEvent = stackEvents.getFirst();
    StackResourceStatusType status = latestEvent.getResourceStatus();
    log.info("Latest status: {}", status);
    log.info("Logical resource id: {}", latestEvent.getLogicalResourceId());
    if (status != null
        && status.toString().contains("COMPLETE")
        && Objects.equals(latestEvent.getLogicalResourceId(), stackName)) {
      log.info("Success or Fails :{}", stackEvents);
      return (status.equals(CREATE_COMPLETE) || status.equals(UPDATE_COMPLETE))
          ? CRUPDATE_SUCCESS
          : CRUPDATE_FAILED;
    }
    return CRUPDATE_IN_PROGRESS;
  }

  public static List<StackEvent> mergeAndSortStackEventList(
      List<StackEvent> actual, List<StackEvent> newEvents) {
    Set<StackEvent> mergedSet = new HashSet<>(actual);
    mergedSet.addAll(newEvents);
    return mergedSet.stream()
        .sorted(
            (e1, e2) -> {
              Instant i1 = e1.getTimestamp();
              Instant i2 = e2.getTimestamp();
              if (i1 == null && i2 == null) return 0;
              if (i1 == null) return 1;
              if (i2 == null) return -1;
              return i2.compareTo(i1);
            })
        .toList();
  }

  private List<StackOutput> mergeStackOutputList(
      List<StackOutput> actual, List<StackOutput> newOutputs) {
    Set<StackOutput> mergedSet = new HashSet<>(actual);
    mergedSet.addAll(newOutputs);
    return mergedSet.stream().toList();
  }

  private void triggerStackResourcesRetrieving(String userId, Stack stack) {
    switch (stack.getType()) {
      case COMPUTE -> {
        eventProducer.accept(
            List.of(
                ComputeStackCrupdateCompleted.builder()
                    .userId(userId)
                    .crupdatedComputeStack(stack)
                    .build()));
      }
      case COMPUTE_PERMISSION, STORAGE_BUCKET, EVENT, STORAGE_DATABASE_SQLITE -> {
        log.error("Get resources for stack type={} not implemented", stack.getType());
      }
    }
  }

  private void sendStackCrupdateFailedMail(String userId, Stack stack) throws AddressException {
    User owner = userService.getUserById(userId);
    String subject = "[JCloudify] Stack creation or update failed";
    String htmlBody =
        String.format("<p>Creation/Update on stack name=%s has failed.</p>", stack.getName());
    mailer.accept(
        new Email(
            new InternetAddress(owner.getEmail()),
            List.of(),
            List.of(),
            subject,
            htmlBody,
            List.of()));
  }
}
