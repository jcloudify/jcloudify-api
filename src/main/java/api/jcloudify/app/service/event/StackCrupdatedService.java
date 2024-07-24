package api.jcloudify.app.service.event;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.STACK_EVENT;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.endpoint.rest.mapper.StackEventMapper;
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
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackEventsResponse;

@Service
@AllArgsConstructor
public class StackCrupdatedService implements Consumer<StackCrupdated> {
  private final CloudformationComponent cloudformationComponent;
  private final ExtendedBucketComponent bucketComponent;
  private final StackEventMapper mapper;
  private final ObjectMapper om;
  private final EventProducer eventProducer;

  @Override
  public void accept(StackCrupdated stackCrupdated) {
    Stack stack = stackCrupdated.getStack();
    String userId = stackCrupdated.getUserId();
    String bucketKey =
        getBucketKey(
            userId, stack.getApplicationId(), stack.getEnvironmentId(), STACK_EVENT, "log.txt");
    String nextToken =
        crupdateStackEvent(stack.getName(), stackCrupdated.getContinuationToken(), bucketKey);
    if (!nextToken.isEmpty()) {
      eventProducer.accept(
          List.of(
              StackCrupdated.builder()
                  .userId(userId)
                  .stack(stack)
                  .continuationToken(nextToken)
                  .build()));
    }
  }

  private String crupdateStackEvent(String stackName, String nextToken, String bucketKey) {
    File stackEventJsonFile = bucketComponent.download(bucketKey);
    DescribeStackEventsResponse response =
        cloudformationComponent.getStackEvents(stackName, nextToken);
    List<StackEvent> stackEvents = response.stackEvents().stream().map(mapper::toRest).toList();
    try {
      if (stackEventJsonFile.exists() && stackEventJsonFile.length() != 0) {
        List<StackEvent> actual = om.readValue(stackEventJsonFile, new TypeReference<>() {});
        stackEvents = mergeAndSortStackEventList(actual, stackEvents);
      } else {
        stackEventJsonFile = new File("log", ".txt");
      }
      om.writeValue(stackEventJsonFile, stackEvents);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
    bucketComponent.upload(stackEventJsonFile, bucketKey);
    return response.nextToken();
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
              return i1.compareTo(i2);
            })
        .toList();
  }
}
