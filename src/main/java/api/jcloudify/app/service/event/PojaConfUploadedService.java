package api.jcloudify.app.service.event;

import static api.jcloudify.app.file.FileType.DEPLOYMENT_FOLDER;
import static api.jcloudify.app.file.FileType.POJA_CONF;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Locale.ROOT;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE;

import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.file.FileUnzipper;
import api.jcloudify.app.mail.Email;
import api.jcloudify.app.mail.Mailer;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.EnvDeploymentConf;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.User;
import api.jcloudify.app.service.AppInstallationService;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.EnvDeploymentConfService;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.UserService;
import api.jcloudify.app.service.api.pojaSam.PojaSamApi;
import api.jcloudify.app.service.github.GithubService;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
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
  private static final String BUILD_TEMPLATE_FILENAME_YML = "template.yml";
  private static final String CF_STACKS_CD_COMPUTE_PERMISSION_YML_PATH =
      "cf-stacks/compute-permission-stack.yml";
  private static final String CF_STACKS_EVENT_STACK_YML_PATH = "cf-stacks/event-stack.yml";
  private static final String CF_STACKS_STORAGE_BUCKET_STACK_YML_PATH =
      "cf-stacks/storage-bucket-stack.yml";
  private final ExtendedBucketComponent bucketComponent;
  private final PojaSamApi pojaSamApi;
  private final GithubService githubService;
  private final AppInstallationService appInstallService;
  private final EnvironmentService envService;
  private final ApplicationService appService;
  private final FileUnzipper unzipper;
  private final EnvDeploymentConfService envDeploymentConfService;
  private final Mailer mailer;
  private final UserService userService;

  @Override
  public void accept(PojaConfUploaded pojaConfUploaded) {
    try {
      Objects.requireNonNull(handlePojaConfUploaded(pojaConfUploaded)).call();
      handleEventSuccess(pojaConfUploaded);
      log.info("Success at PojaConfUploaded");
    } catch (Exception e) {
      log.info("Failure at PojaConfUploaded");
      handleEventFailure(pojaConfUploaded);
      throw new RuntimeException(e);
    }
  }

  private void handleEventFailure(PojaConfUploaded pojaConfUploaded) {
    Application app = appService.getById(pojaConfUploaded.getAppId());
    String userId = app.getUserId();
    User user = userService.getUserById(userId);
    String address = user.getEmail();
    log.info("mailing {}", address);
    mailer.accept(
        new Email(
            internetAddressFrom(address),
            List.of(),
            List.of(),
            "failed push [Jcloudify]",
            "<p> [jcloudifybot] has failed to push the generated code to your repository"
                + app.getGithubRepositoryUrl()
                + " </p>",
            List.of()));
  }

  private void handleEventSuccess(PojaConfUploaded pojaConfUploaded) {
    Application app = appService.getById(pojaConfUploaded.getAppId());
    String userId = app.getUserId();
    User user = userService.getUserById(userId);
    String address = user.getEmail();
    log.info("mailing {}", address);
    mailer.accept(
        new Email(
            internetAddressFrom(address),
            List.of(),
            List.of(),
            "successful push [Jcloudify]",
            "<p> [jcloudifybot] has successfully pushed the generated code to your repository"
                + app.getGithubRepositoryUrl()
                + " </p>",
            List.of()));
  }

  @SneakyThrows
  private static InternetAddress internetAddressFrom(String address) {
    return new InternetAddress(address);
  }

  private Callable<Void> handlePojaConfUploaded(PojaConfUploaded pojaConfUploaded) {
    return () -> {
      String userId = pojaConfUploaded.getUserId();
      String environmentId = pojaConfUploaded.getEnvironmentId();
      String appId = pojaConfUploaded.getAppId();
      Environment env = envService.getById(environmentId);
      Application app = appService.getById(appId);
      var generatedCode = generateCodeFromPojaConf(pojaConfUploaded, userId, appId, environmentId);
      var appInstallationToken = getInstallationToken(pojaConfUploaded, appId);
      UsernamePasswordCredentialsProvider ghCredentialsProvider =
          new UsernamePasswordCredentialsProvider("x-access-token", appInstallationToken);
      var cloneDirPath = createTempDir("github_clone");
      pushChangesFromCodeToAppRepository(
          ghCredentialsProvider,
          pojaConfUploaded.getPojaVersion(),
          app,
          env,
          generatedCode,
          cloneDirPath);
      uploadAndSaveDeploymentFiles(generatedCode, userId, appId, environmentId);
      return null;
    };
  }

  private void uploadAndSaveDeploymentFiles(
      File toUnzip, String userId, String appId, String environmentId) {
    var unzippedCode = createTempDir("unzipped");
    unzip(asZipFile(toUnzip), unzippedCode);
    var tempDirPath = createTempDir("deployment_files");
    var templateBuildFile = unzippedCode.resolve(BUILD_TEMPLATE_FILENAME_YML);
    var computePermissionStackFile = unzippedCode.resolve(CF_STACKS_CD_COMPUTE_PERMISSION_YML_PATH);
    var eventStackFile = unzippedCode.resolve(CF_STACKS_EVENT_STACK_YML_PATH);
    var storageBucketStackFile = unzippedCode.resolve(CF_STACKS_STORAGE_BUCKET_STACK_YML_PATH);
    UUID random = UUID.randomUUID();
    String buildTemplateFilename = "template" + random + ".yml";
    copyFile(templateBuildFile, tempDirPath, buildTemplateFilename);
    String computePermissionStackFilename = "compute-permission" + random + ".yml";
    copyFile(computePermissionStackFile, tempDirPath, computePermissionStackFilename);
    String eventStackFilename = "event-stack" + random + ".yml";
    copyFile(eventStackFile, tempDirPath, eventStackFilename);
    String storageBucketStackFilename = "storage-bucket-stack" + random + ".yml";
    var storageBucketStackFileCopyResult =
        copyFile(storageBucketStackFile, tempDirPath, storageBucketStackFilename);
    bucketComponent.upload(
        tempDirPath.toFile(),
        ExtendedBucketComponent.getBucketKey(userId, appId, environmentId, DEPLOYMENT_FOLDER));
    envDeploymentConfService.save(
        EnvDeploymentConf.builder()
            .envId(environmentId)
            .computePermissionStackFileKey(computePermissionStackFilename)
            .storageBucketStackFileKey(
                storageBucketStackFileCopyResult ? storageBucketStackFilename : null)
            .eventStackFileKey(eventStackFilename)
            .buildTemplateFile(buildTemplateFilename)
            .creationDatetime(Instant.now())
            .build());
  }

  /**
   * copies a file from source to target with the new filename. if copy fails, it throws
   * RuntimeException if file does not exist, it returns false if file exists, it returns true the
   * boolean return value is used to handle non-existing file copies e.g: storage-bucket-stack does
   * not exist if with_file_storage is false in the code_gen conf, hence the need to check whether
   * the file exists or not on copy result
   */
  private static boolean copyFile(Path source, Path target, String newFilename) {
    if (source.toFile().exists()) {
      Path newFilenameResolved = target.resolve(newFilename);
      log.info("Copying {} to {}", source, newFilenameResolved);
      try {
        Files.move(source, newFilenameResolved, REPLACE_EXISTING);
      } catch (IOException e) {
        log.info("failed to copy");
        throw new RuntimeException(e);
      }
      return true;
    }
    log.info("file does not exist {}", source.toAbsolutePath());
    return false;
  }

  private void pushChangesFromCodeToAppRepository(
      UsernamePasswordCredentialsProvider ghCredentialsProvider,
      PojaVersion pojaVersion,
      Application app,
      Environment env,
      File toUnzip,
      Path cloneDirPath) {
    try (Git git =
        Git.cloneRepository()
            .setCredentialsProvider(ghCredentialsProvider)
            .setDirectory(cloneDirPath.toFile())
            .setURI(app.getGithubRepositoryUrl())
            .setDepth(1)
            .call()) {
      String branchName = env.getEnvironmentType().name().toLowerCase(ROOT);
      createAndCheckoutBranchIfNotExists(git, branchName);
      log.info("successfully cloned in {}", cloneDirPath.toAbsolutePath());
      unzip(asZipFile(toUnzip), cloneDirPath);
      configureGitRepositoryGpg(git);
      gitAddAllChanges(git);
      unsignedCommitAsBot(
          git,
          "jcloudify: generate code using v." + pojaVersion.toHumanReadableValue(),
          ghCredentialsProvider);
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
      log.info("Invalid Remote");
      throw new RuntimeException(e);
    } catch (TransportException e) {
      log.info("Transport Exception");
      throw new RuntimeException(e);
    } catch (GitAPIException e) {
      log.info("Git Api Exception");
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  private static ZipFile asZipFile(File toUnzip) {
    return new ZipFile(toUnzip);
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

  /**
   * @param pojaConfUploaded
   * @param userId
   * @param appId
   * @param environmentId
   * @return zip file but we do not use ZipFile type so we can reuse this
   */
  private File generateCodeFromPojaConf(
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
      git.rm().addFilepattern(BUILD_TEMPLATE_FILENAME_YML).call();
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
    PersonIdent author = new PersonIdent("jcloudify[bot]", "jcloudifybot@noreply.com");
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
  private static Path createTempDir(String prefix) {
    return Files.createTempDirectory(prefix);
  }
}
