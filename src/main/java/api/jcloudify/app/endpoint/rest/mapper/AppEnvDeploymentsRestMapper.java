package api.jcloudify.app.endpoint.rest.mapper;

import static api.jcloudify.app.service.event.PojaConfUploadedService.JCLOUDIFY_BOT_USERNAME;

import api.jcloudify.app.endpoint.rest.model.AppEnvDeployment;
import api.jcloudify.app.endpoint.rest.model.GithubMeta;
import api.jcloudify.app.endpoint.rest.model.GithubMetaCommit;
import api.jcloudify.app.endpoint.rest.model.GithubMetaRepo;
import api.jcloudify.app.endpoint.rest.model.GithubUserMeta;
import api.jcloudify.app.repository.model.AppEnvironmentDeployment;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class AppEnvDeploymentsRestMapper {
  public AppEnvDeployment toRest(AppEnvironmentDeployment deployment) {
    GithubMetaRepo repo =
        new GithubMetaRepo()
            .ownerName(deployment.getGhRepoOwnerName())
            .name(deployment.getGhRepoName());
    String ghCommitterName = deployment.getGhCommitterName();
    String ghCommitterAvatarUrl = deployment.getGhCommitterAvatarUrl();
    URI avatarUrl = ghCommitterAvatarUrl == null ? null : URI.create(ghCommitterAvatarUrl);
    GithubUserMeta committer =
        new GithubUserMeta()
            .avatarUrl(avatarUrl)
            .email(deployment.getGhCommitterEmail())
            .githubId(deployment.getGhCommitterId())
            .isJcBot(JCLOUDIFY_BOT_USERNAME.equals(ghCommitterName))
            .login(deployment.getGhCommitterLogin())
            .name(ghCommitterName);
    GithubMetaCommit commit =
        new GithubMetaCommit()
            .branch(deployment.getGhCommitBranch())
            .committer(committer)
            .message(deployment.getGhCommitMessage())
            .sha(deployment.getGhCommitSha())
            .url(URI.create(deployment.getGhCommitUrl()));
    var githubMeta = new GithubMeta().commit(commit).repo(repo);
    return new AppEnvDeployment()
        .applicationId(deployment.getAppId())
        .creationDatetime(deployment.getCreationDatetime())
        .deployedUrl(
            deployment.getDeployedUrl() == null ? null : URI.create(deployment.getDeployedUrl()))
        .environmentId(deployment.getEnv().getId())
        .githubMeta(githubMeta)
        .id(deployment.getId());
  }
}
