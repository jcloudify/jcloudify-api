package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_FAILED;
import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_IN_PROGRESS;
import static api.jcloudify.app.endpoint.event.model.enums.StackCrupdateStatus.CRUPDATE_SUCCESS;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.CREATE_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UPDATE_COMPLETE;
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
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.model.exception.NotImplementedException;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.repository.model.User;
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
import org.springframework.stereotype.Service;

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

  @Override
  public void accept(StackCrupdated stackCrupdated) {
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
    switch (stackCrupdateStatus) {
      case CRUPDATE_IN_PROGRESS ->
          eventProducer.accept(
              List.of(StackCrupdated.builder().userId(userId).stack(stack).build()));
      case CRUPDATE_SUCCESS -> {
        String stackOutputsBucketKey =
            getStackOutputsBucketKey(
                userId,
                stack.getApplicationId(),
                stack.getEnvironmentId(),
                stack.getId(),
                STACK_OUTPUT_FILENAME);
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
    List<StackEvent> stackEvents =
        cloudformationComponent.getStackEvents(stackName).stream().map(mapper::toRest).toList();
    try {
      File stackEventJsonFile;
      if (bucketComponent.doesExist(bucketKey)) {
        stackEventJsonFile = bucketComponent.download(bucketKey);
        List<StackEvent> actual = om.readValue(stackEventJsonFile, new TypeReference<>() {});
        stackEvents = mergeAndSortStackEventList(actual, stackEvents);
      } else {
        stackEventJsonFile = createTempFile("log", ".json");
      }
      om.writeValue(stackEventJsonFile, stackEvents);
      bucketComponent.upload(stackEventJsonFile, bucketKey);
      StackEvent latestEvent = stackEvents.getFirst();
      StackResourceStatusType status = latestEvent.getResourceStatus();
      if (status != null
          && status.toString().contains("COMPLETE")
          && Objects.equals(latestEvent.getLogicalResourceId(), stackName)) {
        return (status.equals(CREATE_COMPLETE) || status.equals(UPDATE_COMPLETE))
            ? CRUPDATE_SUCCESS
            : CRUPDATE_FAILED;
      }
      return CRUPDATE_IN_PROGRESS;
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  private List<StackEvent> mergeAndSortStackEventList(
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
        throw new NotImplementedException("Not implemented");
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
