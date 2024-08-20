package api.jcloudify.app.service.event;

import static api.jcloudify.app.service.StackService.STACK_EVENT_FILENAME;
import static api.jcloudify.app.service.StackService.getStackEventsBucketKey;
import static java.io.File.createTempFile;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.endpoint.rest.mapper.StackMapper;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.repository.model.Stack;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final EventProducer<StackCrupdated> eventProducer;

  @Override
  public void accept(StackCrupdated stackCrupdated) {
    Stack stack = stackCrupdated.getStack();
    String userId = stackCrupdated.getUserId();
    String bucketKey =
        getStackEventsBucketKey(
            userId,
            stack.getApplicationId(),
            stack.getEnvironmentId(),
            stack.getId(),
            STACK_EVENT_FILENAME);
    boolean isLast = crupdateStackEvent(stack.getName(), bucketKey);
    if (!isLast) {
      eventProducer.accept(List.of(StackCrupdated.builder().userId(userId).stack(stack).build()));
    }
  }

  private boolean crupdateStackEvent(String stackName, String bucketKey) {
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
      return Objects.requireNonNull(latestEvent.getResourceStatus())
          .toString()
          .contains("COMPLETE") && Objects.equals(latestEvent.getLogicalResourceId(), stackName);
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
}
