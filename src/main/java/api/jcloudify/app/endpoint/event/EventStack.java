package api.jcloudify.app.endpoint.event;

import static java.lang.System.getenv;

import api.jcloudify.app.PojaGenerated;
import lombok.Getter;

@PojaGenerated
public enum EventStack {
  EVENT_STACK_1(
      getenv("AWS_EVENT_STACK_1_EVENTBRIDGE_BUS"), getenv("AWS_EVENT_STACK_1_SQS_QUEUE_URL")),
  EVENT_STACK_2(
      getenv("AWS_EVENT_STACK_2_EVENTBRIDGE_BUS"), getenv("AWS_EVENT_STACK_2_SQS_QUEUE_URL"));

  @Getter private final String busName;
  @Getter private final String sqsQueueUrl;

  EventStack(String busName, String sqsQueueUrl) {
    this.busName = busName;
    this.sqsQueueUrl = sqsQueueUrl;
  }
}
