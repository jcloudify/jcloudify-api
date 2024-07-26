package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.ApplicationCrupdated;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.service.AppInstallationService;
import api.jcloudify.app.service.github.GithubService;
import api.jcloudify.app.service.github.model.CreateRepoRequestBody;
import api.jcloudify.app.service.github.model.UpdateRepoRequestBody;
import java.net.URI;
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
  private final ApplicationRepository repository;

  @Override
  public void accept(ApplicationCrupdated applicationCrupdated) {
    var persistedInstallation =
        installationService.getById(applicationCrupdated.getInstallationId());
    var token =
        githubService.getInstallationToken(
            persistedInstallation.getGhId(), applicationCrupdated.maxConsumerDuration());
    var owner = persistedInstallation.getOwnerGithubLogin();
    var uri =
        switch (applicationCrupdated.getCrupdateType()) {
          case CREATE -> createRepo(owner, applicationCrupdated, token);
          case UPDATE -> updateRepo(owner, applicationCrupdated, token);
        };
    repository.updateApplicationRepoUrl(applicationCrupdated.getApplicationId(), uri.toString());
  }

  private URI updateRepo(
      String repoOwnerUsername, ApplicationCrupdated applicationCrupdated, String token) {
    var updateRepoResultUri =
        githubService.updateRepoFor(
            new UpdateRepoRequestBody(
                applicationCrupdated.getApplicationRepoName(),
                applicationCrupdated.getDescription(),
                applicationCrupdated.isRepoPrivate(),
                applicationCrupdated.isArchived()),
            applicationCrupdated.getPreviousApplicationRepoName(),
            token,
            repoOwnerUsername);
    log.info("update repo {}", updateRepoResultUri);
    return updateRepoResultUri;
  }

  private URI createRepo(String owner, ApplicationCrupdated applicationCrupdated, String token) {
    var createRepoResultUri =
        githubService.createRepoFor(
            new CreateRepoRequestBody(
                owner,
                applicationCrupdated.getApplicationRepoName(),
                applicationCrupdated.getDescription(),
                applicationCrupdated.isRepoPrivate()),
            token);
    log.info("create repo {}", createRepoResultUri);
    return createRepoResultUri;
  }
}
