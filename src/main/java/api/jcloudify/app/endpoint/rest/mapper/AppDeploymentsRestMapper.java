package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.AppEnvDeployment;
import api.jcloudify.app.endpoint.rest.model.GithubMeta;
import api.jcloudify.app.endpoint.rest.model.GithubUserMeta;
import api.jcloudify.app.repository.model.AppEnvironmentDeployment;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class AppDeploymentsRestMapper {
  public AppEnvDeployment toRest(AppEnvironmentDeployment deployment) {
    var githubMeta =
        new GithubMeta()
            .commitAuthorName(deployment.getGhCommitAuthorName())
            .commitBranch(deployment.getGhCommitBranch())
            .commitMessage(deployment.getGhCommitMessage())
            .commitSha(deployment.getGhCommitSha())
            .isRepoPrivate(deployment.isGhIsRepoPrivate())
            .org(deployment.getGhOrg())
            .repoId(deployment.getGhRepoId())
            .repoName(deployment.getGhRepoName())
            .repoOwnerType(deployment.getGhRepoOwnerType())
            .repoUrl(
                deployment.getGhRepoUrl() == null ? null : URI.create(deployment.getGhRepoUrl()));
    var creator =
        new GithubUserMeta()
            .email(deployment.getCreatorEmail())
            .avatarUrl(
                deployment.getCreatorAvatarUrl() == null
                    ? null
                    : URI.create(deployment.getCreatorAvatarUrl()))
            .githubId(deployment.getCreatorGhId())
            .username(deployment.getCreatorUsername());
    return new AppEnvDeployment()
        .applicationId(deployment.getAppId())
        .creationDatetime(deployment.getCreationDatetime())
        .creator(creator)
        .deployedUrl(
            deployment.getDeployedUrl() == null ? null : URI.create(deployment.getDeployedUrl()))
        .environmentId(deployment.getEnv().getId())
        .githubMeta(githubMeta)
        .id(deployment.getId());
  }
}
