package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.NotifyPojaConfUploaded.Status.FAILURE;
import static api.jcloudify.app.endpoint.event.model.NotifyPojaConfUploaded.Status.SUCCESS;

import api.jcloudify.app.endpoint.event.model.NotifyPojaConfUploaded;
import api.jcloudify.app.mail.Email;
import api.jcloudify.app.mail.Mailer;
import api.jcloudify.app.model.User;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.UserService;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class NotifyPojaConfUploadedService implements Consumer<NotifyPojaConfUploaded> {
  private final Mailer mailer;
  private final ApplicationService appService;
  private final UserService userService;
  private final EnvironmentService environmentService;

  @Override
  public void accept(NotifyPojaConfUploaded notifyPojaConfUploaded) {
    Application app = appService.getById(notifyPojaConfUploaded.getAppId());
    var env = environmentService.getById(notifyPojaConfUploaded.getEnvId());
    String userId = app.getUserId();
    User user = userService.getUserById(userId);
    String address = user.getEmail();
    var status = notifyPojaConfUploaded.getStatus();
    log.info("mailing {}", address);
    if (FAILURE.equals(status)) {
      handleFailure(address, app, env);
    } else if (SUCCESS.equals(status)) {
      handleSuccess(address, app, env);
    }
  }

  private void handleFailure(String address, Application app, Environment env) {
    mailer.accept(
        new Email(
            internetAddressFrom(address),
            List.of(),
            List.of(),
            "failed push [Jcloudify]",
            "<p> [jcloudifybot] has failed to push the generated code to your repository"
                + app.getGithubRepositoryUrl()
                + " branch "
                + env.getEnvironmentType()
                + " </p>",
            List.of()));
  }

  private void handleSuccess(String address, Application app, Environment env) {
    mailer.accept(
        new Email(
            internetAddressFrom(address),
            List.of(),
            List.of(),
            "successful push [Jcloudify]",
            "<p> [jcloudifybot] has successfully pushed the generated code to your repository "
                + app.getGithubRepositoryUrl()
                + " branch "
                + env.getEnvironmentType()
                + " </p>",
            List.of()));
  }

  @SneakyThrows
  private static InternetAddress internetAddressFrom(String address) {
    return new InternetAddress(address);
  }
}
