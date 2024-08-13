package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.AppEnvComputeDeployRequested;
import api.jcloudify.app.endpoint.event.model.AppEnvDeployRequested;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.service.ApplicationService;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppEnvDeployRequestedService implements Consumer<AppEnvDeployRequested> {
  private final EventProducer<AppEnvComputeDeployRequested> eventProducer;
  private final ApplicationService appService;

  @Override
  public void accept(AppEnvDeployRequested appEnvDeployRequested) {
    // TODO: deploy other stacks needed before deploying compute
    var app = appService.getById(appEnvDeployRequested.getAppId());
    BuiltEnvInfo builtEnvInfo = appEnvDeployRequested.getBuiltEnvInfo();
    eventProducer.accept(
        List.of(
            AppEnvComputeDeployRequested.builder()
                .appName(app.getName())
                .formattedBucketKey(builtEnvInfo.getFormattedBucketKey())
                .requestInstant(Instant.now())
                .environmentType(builtEnvInfo.getEnvironmentType())
                .build()));
  }
}
