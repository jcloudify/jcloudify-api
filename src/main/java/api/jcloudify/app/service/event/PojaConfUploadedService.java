package api.jcloudify.app.service.event;

import static api.jcloudify.app.file.FileType.POJA_CONF;
import static java.util.Locale.ROOT;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE;

import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.file.FileUnzipper;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.AppInstallationService;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.api.pojaSam.PojaSamApi;
import api.jcloudify.app.service.github.GithubService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PojaConfUploadedService implements Consumer<PojaConfUploaded> {
  private final ExtendedBucketComponent bucketComponent;
  private final PojaSamApi pojaSamApi;
  private final GithubService githubService;
  private final AppInstallationService appInstallService;
  private final EnvironmentService envService;
  private final ApplicationService appService;
  private final FileUnzipper unzipper;

  @Override
  public void accept(PojaConfUploaded pojaConfUploaded) {
    String userId = pojaConfUploaded.getUserId();
    String environmentId = pojaConfUploaded.getEnvironmentId();
    String appId = pojaConfUploaded.getAppId();
    Environment env = envService.getById(environmentId);
    Application app = appService.getById(appId);
    var generatedCode = generateCodeFromPojaConf(pojaConfUploaded, userId, appId, environmentId);
    var appInstallationToken = getInstallationToken(pojaConfUploaded, appId);
    UsernamePasswordCredentialsProvider ghCredentialsProvider =
        new UsernamePasswordCredentialsProvider("x-access-token", appInstallationToken);
    pushChangesFromCodeToAppRepository(
        ghCredentialsProvider, pojaConfUploaded.getPojaVersion(), app, env, generatedCode);
  }

  private void pushChangesFromCodeToAppRepository(
      UsernamePasswordCredentialsProvider ghCredentialsProvider,
      PojaVersion pojaVersion,
      Application app,
      Environment env,
      ZipFile toUnzip) {
    var cloneDir = createTempDirForGithub();
    try (Git git =
        Git.cloneRepository()
            .setCredentialsProvider(ghCredentialsProvider)
            .setDirectory(cloneDir.toFile())
            .setURI(app.getRepoHttpUrl())
            .setDepth(1)
            .call()) {
      String branchName = env.getEnvironmentType().name().toLowerCase(ROOT);
      createAndCheckoutBranchIfNotExists(git, branchName);
      log.info("successfully cloned in {}", cloneDir.toAbsolutePath());
      unzip(toUnzip, cloneDir);
      configureGitRepositoryGpg(git);
      gitAddAllChanges(git);
      unsignedCommitAsBot(
          git, "poja-upgrade: " + pojaVersion.toHumanReadableValue(), ghCredentialsProvider);
      var results = git.push().setCredentialsProvider(ghCredentialsProvider).call();
      for (PushResult r : results) {
        for (RemoteRefUpdate update : r.getRemoteUpdates()) {
          log.info("Having results: " + update);
          if (update.getStatus() != OK && update.getStatus() != UP_TO_DATE) {
            String errorMessage = "Push failed: " + update.getStatus();
            throw new RuntimeException(errorMessage);
          }
        }
      }
    } catch (InvalidRemoteException e) {
      throw new RuntimeException(e);
    } catch (TransportException e) {
      throw new RuntimeException(e);
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private void unzip(ZipFile downloaded, Path destination) {
    unzipper.apply(downloaded, destination);
  }

  private static void createAndCheckoutBranchIfNotExists(Git git, String branchName) {
    String formattedBranchName = "refs/heads/" + branchName;
    try {
      if (git.branchList().call().stream()
          .noneMatch(a -> a.getName().equals(formattedBranchName))) {
        git.branchCreate().setName(branchName).call();
      }
      git.checkout().setName(branchName).call();
      log.info("successfully checked out branch {}", branchName);
    } catch (RefNotFoundException | RefAlreadyExistsException | InvalidRefNameException e) {
      // unreachable
      throw new RuntimeException(e);
    } catch (CheckoutConflictException e) {
      throw new RuntimeException(e);
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private ZipFile generateCodeFromPojaConf(
      PojaConfUploaded pojaConfUploaded, String userId, String appId, String environmentId) {
    String formattedFilename =
        ExtendedBucketComponent.getBucketKey(
            userId, appId, environmentId, POJA_CONF, pojaConfUploaded.getFilename());
    log.info("downloading pojaConfFile: {}", formattedFilename);
    var pojaConfFile = bucketComponent.download(formattedFilename);
    log.info("downloaded pojaConfFile: {}", pojaConfFile.getName());
    return pojaSamApi.genCodeTo(pojaConfUploaded.getPojaVersion(), pojaConfFile);
  }

  private static void gitAddAllChanges(Git git) {
    try {
      git.add().addFilepattern(".").call();
      git.rm().addFilepattern("cf-stacks/").call();
      git.rm().addFilepattern("poja-custom-java-env-vars.txt").call();
      git.rm().addFilepattern("poja-custom-java-repositories.txt").call();
      git.rm().addFilepattern("poja-custom-java-deps.txt").call();
      git.rm().addFilepattern("template.yml").call();
      git.rm().addFilepattern("poja.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-compute.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-compute-permission.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-event.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-storage-bucket.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-storage-database.yml").call();
      git.rm().addFilepattern(".github/workflows/health-check-email.yml").call();
      git.rm().addFilepattern(".github/workflows/health-check-infra.yml").call();
      git.rm().addFilepattern(".github/workflows/health-check-poja.yml").call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  private static void configureGitRepositoryGpg(Git git) {
    StoredConfig storedConfig = git.getRepository().getConfig();
    storedConfig.setString("gpg", null, "format", "openpgp");
  }

  private static void unsignedCommitAsBot(
      Git git, String commitMessage, CredentialsProvider credentialsProvider) {
    PersonIdent author =
        new PersonIdent("jcloudify[bot]", "jcloudifybot@noreply.comjcloudifybot@noreply.com");
    try {
      git.commit()
          .setMessage(commitMessage)
          .setAuthor(author)
          .setCommitter(author)
          .setCredentialsProvider(credentialsProvider)
          .setSign(false)
          .call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private String getInstallationToken(PojaConfUploaded pojaConfUploaded, String appId) {
    var app = appService.getById(appId);
    var appInstallation = appInstallService.getById(app.getInstallationId());
    return githubService.getInstallationToken(
        appInstallation.getGhId(), pojaConfUploaded.maxConsumerDuration());
  }

  @SneakyThrows
  private static Path createTempDirForGithub() {
    return Files.createTempDirectory("github_clone");
  }
}
