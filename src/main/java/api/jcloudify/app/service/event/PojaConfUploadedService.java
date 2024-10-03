package api.jcloudify.app.service.event;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.DEPLOYMENT_FILE;
import static api.jcloudify.app.file.FileType.POJA_CONF;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Locale.ROOT;
import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE;

import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.file.FileUnzipper;
import api.jcloudify.app.mail.Email;
import api.jcloudify.app.mail.Mailer;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.User;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.EnvDeploymentConf;
import api.jcloudify.app.repository.model.Environment;
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
import java.nio.file.Paths;
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
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
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
  private static final String CF_STACKS_STORAGE_SQLITE_STACK_YML_PATH =
      "cf-stacks/storage-efs-stack.yml";
  private static final String CD_COMPUTE_BUCKET_KEY = "poja-templates/cd-compute.yml";
  private static final String REMOTE_ORIGIN = "origin";
  private static final RefSpec FETCH_ALL_AND_UPDATE_REFSPEC =
      new RefSpec("+refs/heads/*:refs/heads/*");
  public static final String JCLOUDIFY_BOT_USERNAME = "jcloudify[bot]";
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
      log.error("Error Message: {}", e.getMessage());
      log.error("Stacktrace: {}", (Object) e.getStackTrace());
      log.error("Error Cause: {}", e.getCause().toString());
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
      uploadAndSaveDeploymentFiles(generatedCode, pojaConfUploaded);
      return null;
    };
  }

  private void uploadAndSaveDeploymentFiles(File toUnzip, PojaConfUploaded pojaConfUploaded) {
    var userId = pojaConfUploaded.getUserId();
    var appId = pojaConfUploaded.getAppId();
    var environmentId = pojaConfUploaded.getEnvironmentId();

    var unzippedCode = createTempDir("unzipped");
    unzip(asZipFile(toUnzip), unzippedCode);
    var tempDirPath = createTempDir("deployment_files");
    var templateBuildFile = unzippedCode.resolve(BUILD_TEMPLATE_FILENAME_YML);
    var computePermissionStackFile = unzippedCode.resolve(CF_STACKS_CD_COMPUTE_PERMISSION_YML_PATH);
    var eventStackFile = unzippedCode.resolve(CF_STACKS_EVENT_STACK_YML_PATH);
    var storageBucketStackFile = unzippedCode.resolve(CF_STACKS_STORAGE_BUCKET_STACK_YML_PATH);
    var storageSqliteStackFile = unzippedCode.resolve(CF_STACKS_STORAGE_SQLITE_STACK_YML_PATH);
    UUID random = UUID.randomUUID();
    String buildTemplateFilename = "template" + random + ".yml";
    copyFile(templateBuildFile, tempDirPath, buildTemplateFilename);
    String computePermissionStackFilename = "compute-permission" + random + ".yml";
    copyFile(computePermissionStackFile, tempDirPath, computePermissionStackFilename);
    String eventStackFilename = "event-stack" + random + ".yml";
    var eventStackFileCopyResult = copyFile(eventStackFile, tempDirPath, eventStackFilename);
    String storageBucketStackFilename = "storage-bucket-stack" + random + ".yml";
    var storageBucketStackFileCopyResult =
        copyFile(storageBucketStackFile, tempDirPath, storageBucketStackFilename);
    String storageSqliteStackFilename = "storage-efs-stack" + random + ".yml";
    var storageSqliteStackFileCopyResult =
        copyFile(storageSqliteStackFile, tempDirPath, storageSqliteStackFilename);
    bucketComponent.upload(
        tempDirPath.toFile(), getBucketKey(userId, appId, environmentId, DEPLOYMENT_FILE));
    envDeploymentConfService.save(
        EnvDeploymentConf.builder()
            .envId(environmentId)
            .computePermissionStackFileKey(computePermissionStackFilename)
            .storageBucketStackFileKey(
                storageBucketStackFileCopyResult ? storageBucketStackFilename : null)
            .eventStackFileKey(eventStackFileCopyResult ? eventStackFilename : null)
            .storageDatabaseSqliteStackFileKey(
                storageSqliteStackFileCopyResult ? storageSqliteStackFilename : null)
            .buildTemplateFile(buildTemplateFilename)
            .creationDatetime(Instant.now())
            .pojaConfFileKey(pojaConfUploaded.getFilename())
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

  /**
   * ensures repository branches already exist before the real clone and the real code changes.
   * should be do-able in one go but it _SEEMS_ like git checkout doesn't handle it properly
   */
  private void prepareRepository(
      UsernamePasswordCredentialsProvider ghCredentialsProvider, Application app, Environment env) {
    var cloneDirPath = createTempDir("pre-clone");
    String githubRepositoryUrl = app.getGithubRepositoryUrl();
    log.info("BEGIN [branch-check] pre-cloning {}", githubRepositoryUrl);
    String branchName = formatShortBranchName(env);
    try (Git git =
        Git.cloneRepository()
            .setCredentialsProvider(ghCredentialsProvider)
            .setDirectory(cloneDirPath.toFile())
            .setURI(githubRepositoryUrl)
            .setDepth(1)
            .setNoCheckout(true)
            .call()) {
      if (doesBranchExistInRemote(git, branchName)) {
        log.info("branch already exists");
        return;
      }
      log.info("branch does not exist");
      createBranch(git, branchName);
      pushAndCheckResult(ghCredentialsProvider, branchName, git);
    } catch (InvalidRemoteException e) {
      throw new RuntimeException(e);
    } catch (TransportException e) {
      throw new RuntimeException(e);
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
    log.info("END [branch-check] pre-cloning {}", githubRepositoryUrl);
  }

  private static void createBranch(Git git, String branchName) {
    try {
      git.checkout().setCreateBranch(true).setName(branchName).setUpstreamMode(SET_UPSTREAM).call();
      log.info("successfully created and checked out branch {}", branchName);
    } catch (RefNotFoundException | RefAlreadyExistsException | InvalidRefNameException ignored) {
      // unreachable because we check for branch existence in remote first then create it with name
      // "PREPROD" or "PROD" which are very valid.
    } catch (CheckoutConflictException ignored) {
      // unreachable because this function creates a branch from an existing branch via checkout
      // command, hence no conflict is possible
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean doesBranchExistInRemote(Git git, String branchName) {
    String formattedBranchName = getFormattedBranchName(branchName);
    try {
      FetchResult fetchResult =
          git.fetch().setRemote(REMOTE_ORIGIN).setRefSpecs(FETCH_ALL_AND_UPDATE_REFSPEC).call();
      // Check if the branch exists in the remote repository
      return fetchResult.getAdvertisedRefs().stream()
          .anyMatch(ref -> ref.getName().equals(formattedBranchName));
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private void pushAndCheckResult(
      UsernamePasswordCredentialsProvider ghCredentialsProvider, String branchName, Git git)
      throws GitAPIException {
    var results =
        git.push()
            .setRefSpecs(new RefSpec(getFormattedBranchName(branchName)))
            .setCredentialsProvider(ghCredentialsProvider)
            .call();
    for (PushResult r : results) {
      for (RemoteRefUpdate update : r.getRemoteUpdates()) {
        log.info("Having results: " + update);
        if (update.getStatus() != OK && update.getStatus() != UP_TO_DATE) {
          String errorMessage = "Push failed: " + update.getStatus();
          throw new RuntimeException(errorMessage);
        }
      }
    }
  }

  private static String formatShortBranchName(Environment env) {
    return env.getEnvironmentType().name().toLowerCase(ROOT);
  }

  private void pushChangesFromCodeToAppRepository(
      UsernamePasswordCredentialsProvider ghCredentialsProvider,
      PojaVersion pojaVersion,
      Application app,
      Environment env,
      File toUnzip,
      Path cloneDirPath) {
    prepareRepository(ghCredentialsProvider, app, env);
    String branchName = formatShortBranchName(env);
    try (Git git =
        Git.cloneRepository()
            .setCredentialsProvider(ghCredentialsProvider)
            .setDirectory(cloneDirPath.toFile())
            .setURI(app.getGithubRepositoryUrl())
            .setBranch(branchName)
            .setDepth(1)
            .call()) {
      log.info("successfully cloned in {}", cloneDirPath.toAbsolutePath());
      unzip(asZipFile(toUnzip), cloneDirPath);
      configureGitRepositoryGpg(git);
      getAndConfigureCdCompute(cloneDirPath);
      gitAddAllChanges(git);
      unsignedCommitAsBot(
          git,
          "jcloudify: generate code using v." + pojaVersion.toHumanReadableValue(),
          ghCredentialsProvider);
      pushAndCheckResult(ghCredentialsProvider, branchName, git);
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

  private static String getFormattedBranchName(String branchName) {
    return "refs/heads/" + branchName;
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
        getBucketKey(userId, appId, environmentId, POJA_CONF, pojaConfUploaded.getFilename());
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
    PersonIdent author = new PersonIdent(JCLOUDIFY_BOT_USERNAME, "jcloudifybot@noreply.com");
    try {
      git.commit()
          .setMessage(commitMessage)
          .setAuthor(author)
          .setAllowEmpty(true)
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

  private void getAndConfigureCdCompute(Path clonedDirPath) {
    File rawCdComputeFile = bucketComponent.download(CD_COMPUTE_BUCKET_KEY);
    String placeHolder = "<?env>";
    Path ghWorkflowDir = Path.of(clonedDirPath + "/.github/workflows/cd-compute.yml");
    String env = System.getenv("ENV");
    try {
      Path rawFilePath = Paths.get(rawCdComputeFile.toURI());
      String fileContent = new String(Files.readAllBytes(rawFilePath));
      fileContent = fileContent.replace(placeHolder, env);
      Files.write(rawFilePath, fileContent.getBytes(), TRUNCATE_EXISTING);
      Files.copy(rawFilePath, ghWorkflowDir, REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("Error occurred during configuration of cd-compute-file.");
      throw new InternalServerErrorException(e);
    }
  }

  @SneakyThrows
  private static Path createTempDir(String prefix) {
    return Files.createTempDirectory(prefix);
  }
}
