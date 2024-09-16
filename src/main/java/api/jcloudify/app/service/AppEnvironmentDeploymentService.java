package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvironmentDeploymentRepository;
import api.jcloudify.app.repository.jpa.dao.EnvironmentDeploymentDao;
import api.jcloudify.app.repository.model.AppEnvironmentDeployment;
import api.jcloudify.app.repository.model.EnvDeploymentConf;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.appEnvConfigurer.AppEnvConfigurerService;
import api.jcloudify.app.service.github.model.GhGetCommitResponse;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppEnvironmentDeploymentService {
  private final EnvironmentDeploymentRepository repository;
  private final EnvironmentDeploymentDao dao;
  private final EnvDeploymentConfService envDeploymentConfService;
  private final AppEnvConfigurerService appEnvConfigurerService;
  private final AppInstallationService appInstallationService;
  private final GithubComponent githubComponent;

  public AppEnvironmentDeployment save(
      String repoOwnerName,
      String repoName,
      String installationId,
      String ghSha,
      Environment environment,
      EnvDeploymentConf envDeploymentConf) {
    var installation = appInstallationService.getById(installationId);
    GhGetCommitResponse commitInfo =
        githubComponent.getCommitInfo(repoOwnerName, installation.getGhId(), repoName, ghSha);
    GhGetCommitResponse.GhUser committer = commitInfo.committer();
    return save(
        AppEnvironmentDeployment.builder()
            .appId(environment.getApplicationId())
            .deployedUrl(null)
            .env(environment)
            .envDeplConfId(envDeploymentConf.getId())
            .ghCommitMessage(commitInfo.commit().message())
            .ghCommitSha(commitInfo.sha())
            .ghCommitUrl(commitInfo.commit().url().toString())
            .ghCommitterAvatarUrl(committer == null ? null : committer.avatarUrl().toString())
            .ghCommitterEmail(commitInfo.commit().committer().email())
            .ghCommitterId(committer == null ? null : committer.id())
            .ghCommitterLogin(committer == null ? null : committer.login())
            .ghCommitterName(commitInfo.commit().committer().name())
            .ghCommitterType(committer == null ? null : committer.type())
            .ghRepoName(repoName)
            .ghRepoOwnerName(repoOwnerName)
            .build());
  }

  public AppEnvironmentDeployment save(AppEnvironmentDeployment appEnvironmentDeployment) {
    return repository.save(appEnvironmentDeployment);
  }

  public AppEnvironmentDeployment getById(String id) {
    return findById(id)
        .orElseThrow(
            () -> new NotFoundException("AppEnvironmentDeployment.Id = " + id + " not found."));
  }

  public Optional<AppEnvironmentDeployment> findById(String id) {
    return repository.findById(id);
  }

  public Page<AppEnvironmentDeployment> findAllByCriteria(
      String userId,
      String appId,
      EnvironmentType envType,
      Instant startDatetime,
      Instant endDatetime,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var data =
        dao.findAllByCriteria(
            appId,
            envType,
            startDatetime,
            endDatetime,
            PageRequest.of(page.getValue() - 1, pageSize.getValue()));
    return new Page<>(page, pageSize, data);
  }

  @Transactional
  public OneOfPojaConf getConfig(String userId, String appId, String deploymentId) {
    var persisted = getById(deploymentId);
    var deploymentConf = envDeploymentConfService.getById(persisted.getEnvDeplConfId());
    String pojaConfFileKey = deploymentConf.getPojaConfFileKey();
    return appEnvConfigurerService.readConfig(
        userId, appId, persisted.getEnv().getId(), pojaConfFileKey);
  }
}
