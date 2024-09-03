package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.CrupdateLogStreamTriggered;
import api.jcloudify.app.service.LambdaFunctionLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@AllArgsConstructor
@Slf4j
public class CrupdateLogStreamTriggeredService implements Consumer<CrupdateLogStreamTriggered> {
    private final LambdaFunctionLogService lambdaFunctionLogService;
    @Override
    public void accept(CrupdateLogStreamTriggered crupdateLogStreamTriggered) {
        String logGroupName = crupdateLogStreamTriggered.getLogGroupName();
        log.info("Crupdating log group name={} log streams", logGroupName);
        lambdaFunctionLogService.crupdateLogStreams(logGroupName, crupdateLogStreamTriggered.getBucketKey());
    }
}
