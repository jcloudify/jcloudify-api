package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.CrupdateLogStreamEventTriggered;
import api.jcloudify.app.service.LambdaFunctionLogService;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CrupdateLogStreamEventTriggeredService
    implements Consumer<CrupdateLogStreamEventTriggered> {
  private final LambdaFunctionLogService lambdaFunctionLogService;

  @Override
  public void accept(CrupdateLogStreamEventTriggered crupdateLogStreamEventTriggered) {
    String logGroupName = crupdateLogStreamEventTriggered.getLogGroupName();
    String logStreamName = crupdateLogStreamEventTriggered.getLogStreamName();
    log.info(
        "Crupdating log events of log group name={} log streams name={}",
        logGroupName,
        logStreamName);
    lambdaFunctionLogService.crupdateLogStreamEvents(
        logGroupName, logStreamName, crupdateLogStreamEventTriggered.getBucketKey());
  }
}
