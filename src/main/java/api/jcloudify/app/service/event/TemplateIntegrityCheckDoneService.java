package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum.NOT_READY;
import static api.jcloudify.app.endpoint.event.model.enums.TemplateIntegrityStatus.AUTHENTIC;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.*;
import static java.time.Instant.now;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.AppEnvDeployRequested;
import api.jcloudify.app.endpoint.event.model.PojaEvent;
import api.jcloudify.app.endpoint.event.model.TemplateIntegrityCheckDone;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.mail.Email;
import api.jcloudify.app.mail.Mailer;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.UserService;
import api.jcloudify.app.service.workflows.DeploymentStateService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TemplateIntegrityCheckDoneService implements Consumer<TemplateIntegrityCheckDone> {
  private final EventProducer<PojaEvent> eventProducer;
  private final UserService userService;
  private final ApplicationService applicationService;
  private final EnvironmentService environmentService;
  private final Mailer mailer;
  private final DeploymentStateService deploymentStateService;

  @Override
  public void accept(TemplateIntegrityCheckDone templateIntegrityCheckDone) {
    String userId = templateIntegrityCheckDone.getUserId();
    String appId = templateIntegrityCheckDone.getAppId();
    String envId = templateIntegrityCheckDone.getEnvId();
    String appEnvDeploymentId = templateIntegrityCheckDone.getAppEnvDeploymentId();
    if (AUTHENTIC.equals(templateIntegrityCheckDone.getStatus())) {
      handleAuthenticTemplateFile(
          userId,
          envId,
          appId,
          templateIntegrityCheckDone.getBuiltEnvInfo(),
          templateIntegrityCheckDone.getDeploymentConfId(),
          templateIntegrityCheckDone.getBuiltProjectBucketKey(),
          templateIntegrityCheckDone.getAppEnvDeploymentId());
    } else {
      handleCorruptedTemplateFile(userId, appId, envId, appEnvDeploymentId);
    }
  }

  private void handleCorruptedTemplateFile(
      String userId, String appId, String envId, String appEnvDeploymentId) {
    User user = userService.getUserById(userId);
    Application application = applicationService.getById(appId);
    Environment environment = environmentService.getById(envId);
    deploymentStateService.save(appEnvDeploymentId, TEMPLATE_FILE_CHECK_FAILED);
    String address = user.getEmail();
    log.info("Mailing adress={}", address);
    try {
      mailer.accept(
          new Email(
              new InternetAddress(address),
              List.of(),
              List.of(),
              "Deployment not initiated [Jcloudify]",
              "<p> Deployment triggered from repository "
                  + application.getGithubRepositoryName()
                  + ", on branch "
                  + environment.getEnvironmentType().toString().toLowerCase()
                  + " has failed. </p>"
                  + "<strong> Cause: </strong>"
                  + "<p> template.yml file has been altered during deployment initiation. </p>",
              List.of()));
    } catch (AddressException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleAuthenticTemplateFile(
      String userId,
      String envId,
      String appId,
      BuiltEnvInfo builtEnvInfo,
      String deploymentConfId,
      String builtProjectBucketKey,
      String appEnvDeploymentId) {
    eventProducer.accept(
        List.of(
            AppEnvDeployRequested.builder()
                .userId(userId)
                .builtEnvInfo(builtEnvInfo)
                .deploymentConfId(deploymentConfId)
                .requestInstant(now())
                .builtZipFormattedFilekey(builtProjectBucketKey)
                .envId(envId)
                .appId(appId)
                .independentStacksStates(NOT_READY)
                .appEnvDeploymentId(appEnvDeploymentId)
                .build()));
    deploymentStateService.save(appEnvDeploymentId, INDEPENDENT_STACKS_DEPLOYMENT_INITIATED);
  }
}
