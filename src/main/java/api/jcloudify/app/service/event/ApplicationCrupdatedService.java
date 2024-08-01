package api.jcloudify.app.service.event;

import api.jcloudify.app.endpoint.event.model.ApplicationCrupdated;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.service.AppInstallationService;
import api.jcloudify.app.service.github.GithubService;
import api.jcloudify.app.service.github.model.CreateRepoRequestBody;
import api.jcloudify.app.service.github.model.CreateRepoResponse;
import api.jcloudify.app.service.github.model.UpdateRepoRequestBody;
import api.jcloudify.app.service.github.model.UpdateRepoResponse;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationCrupdatedService implements Consumer<ApplicationCrupdated> {
  private final GithubService githubService;
  private final AppInstallationService installationService;
  private final ApplicationRepository repository;

  @Override
  @Transactional
  public void accept(ApplicationCrupdated applicationCrupdated) {
    var persistedInstallation =
        installationService.getById(applicationCrupdated.getInstallationId());
    var token =
        githubService.getInstallationToken(
            persistedInstallation.getGhId(), applicationCrupdated.maxConsumerDuration());
    var owner = persistedInstallation.getOwnerGithubLogin();

    switch (applicationCrupdated.getCrupdateType()) {
      case CREATE -> {
        var created = createRepo(owner, applicationCrupdated, token);
        repository.updateApplicationRepoUrl(
            applicationCrupdated.getApplicationId(),
            String.valueOf(created.htmlUrl()),
            created.id());
      }
      case UPDATE -> {
        var updated = updateRepo(owner, applicationCrupdated, token);
        repository.updateApplicationRepoUrl(
            applicationCrupdated.getApplicationId(),
            String.valueOf(updated.htmlUrl()),
            updated.id());
      }
    }
  }

  private UpdateRepoResponse updateRepo(
      String repoOwnerUsername, ApplicationCrupdated applicationCrupdated, String token) {
    var updateRepoResponse =
        githubService.updateRepoFor(
            new UpdateRepoRequestBody(
                applicationCrupdated.getApplicationRepoName(),
                applicationCrupdated.getDescription(),
                applicationCrupdated.isRepoPrivate(),
                applicationCrupdated.isArchived()),
            applicationCrupdated.getPreviousApplicationRepoName(),
            token,
            repoOwnerUsername);
    log.info("update repo {}", updateRepoResponse);
    return updateRepoResponse;
  }

  private CreateRepoResponse createRepo(
      String owner, ApplicationCrupdated applicationCrupdated, String token) {
    var createRepoResponse =
        githubService.createRepoFor(
            new CreateRepoRequestBody(
                owner,
                applicationCrupdated.getApplicationRepoName(),
                applicationCrupdated.getDescription(),
                applicationCrupdated.isRepoPrivate()),
            token);
    log.info("create repo {}", createRepoResponse);
    return createRepoResponse;
  }
}
