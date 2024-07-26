package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.ApplicationCrupdated;
import api.jcloudify.app.service.AppInstallationService;
import api.jcloudify.app.service.github.GithubService;
import api.jcloudify.app.service.github.model.CreateRepoRequestBody;
import api.jcloudify.app.service.github.model.UpdateRepoRequestBody;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationCrupdatedService implements Consumer<ApplicationCrupdated> {
  private final GithubService githubService;
  private final AppInstallationService installationService;

  @Override
  public void accept(ApplicationCrupdated applicationCrupdated) {
    var persistedInstallation =
        installationService.getById(applicationCrupdated.getInstallationId());
    var token =
        githubService.getInstallationToken(
            persistedInstallation.getGhId(), applicationCrupdated.maxConsumerDuration());
    var owner = persistedInstallation.getOwnerGithubLogin();
    switch (applicationCrupdated.getCrupdateType()) {
      case CREATE -> createRepo(owner, applicationCrupdated, token);
      case UPDATE -> updateRepo(owner, applicationCrupdated, token);
    }
  }

  private void updateRepo(
      String repoOwnerUsername, ApplicationCrupdated applicationCrupdated, String token) {
    var updateRepo =
        githubService.updateRepoFor(
            new UpdateRepoRequestBody(
                applicationCrupdated.getApplicationRepoName(),
                applicationCrupdated.getDescription(),
                applicationCrupdated.isRepoPrivate(),
                applicationCrupdated.isArchived()),
            applicationCrupdated.getPreviousApplicationRepoName(),
            token,
            repoOwnerUsername);
    log.info("update repo {}", updateRepo);
  }

  private void createRepo(String owner, ApplicationCrupdated applicationCrupdated, String token) {
    var createRepo =
        githubService.createRepoFor(
            new CreateRepoRequestBody(
                owner,
                applicationCrupdated.getApplicationRepoName(),
                applicationCrupdated.getDescription(),
                applicationCrupdated.isRepoPrivate()),
            token);
    log.info("create repo {}", createRepo);
  }
}
