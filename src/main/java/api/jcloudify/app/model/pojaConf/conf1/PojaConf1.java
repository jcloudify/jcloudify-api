package api.jcloudify.app.model.pojaConf.conf1;

import static api.jcloudify.app.model.PojaVersion.POJA_1;

import api.jcloudify.app.endpoint.rest.model.ComputeConf1;
import api.jcloudify.app.endpoint.rest.model.ConcurrencyConf1;
import api.jcloudify.app.endpoint.rest.model.DatabaseConf1;
import api.jcloudify.app.endpoint.rest.model.GenApiClient1;
import api.jcloudify.app.endpoint.rest.model.GeneralPojaConf1;
import api.jcloudify.app.endpoint.rest.model.Integration1;
import api.jcloudify.app.endpoint.rest.model.MailingConf1;
import api.jcloudify.app.endpoint.rest.model.TestingConf1;
import api.jcloudify.app.endpoint.rest.model.WithQueuesNbEnum;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.pojaConf.NetworkingConfig;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@JsonPropertyOrder(alphabetic = true)
@Builder
public record PojaConf1(
    @JsonProperty("general") General general,
    @JsonProperty("integration") Integration integration,
    @JsonProperty("gen_api_client") GenApiClient genApiClient,
    @JsonProperty("concurrency") Concurrency concurrency,
    @JsonProperty("compute") Compute compute,
    @JsonProperty("emailing") MailingConf mailing,
    @JsonProperty("testing") TestingConf testing,
    @JsonProperty("database") Database database,
    @JsonProperty("networking") NetworkingConfig networking)
    implements PojaConf {

  @JsonGetter
  public String version() {
    return getVersion().toHumanReadableValue();
  }

  @Override
  public PojaVersion getVersion() {
    return POJA_1;
  }

  public record General(
      @JsonProperty("app_name") String appName,
      @JsonProperty("with_snapstart") Boolean withSnapstart,
      @JsonProperty("with_queues_nb") WithQueuesNbEnum withQueuesNb,
      @JsonProperty("package_full_name") String packageFullName,
      @JsonProperty("custom_java_repositories") List<String> customJavaRepositories,
      @JsonProperty("custom_java_deps") List<String> customJavaDeps,
      @JsonProperty("custom_java_env_vars") Map<String, String> customJavaEnvVars,
      @JsonProperty("poja_python_repository_name") String pojaPythonRepositoryName,
      @JsonProperty("poja_python_repository_domain") String pojaPythonRepositoryDomain,
      @JsonProperty("poja_domain_owner") String pojaDomainOwner,
      @JsonProperty(JSON_PROPERTY_CLI_VERSION) String cliVersion) {
    public static final String JSON_PROPERTY_CLI_VERSION = "cli_version";

    @Builder
    public General(
        GeneralPojaConf1 rest,
        String pojaPythonRepositoryName,
        String pojaPythonRepositoryDomain,
        String pojaDomainOwner) {
      this(
          rest.getAppName(),
          rest.getWithSnapstart(),
          rest.getWithQueuesNb(),
          rest.getPackageFullName(),
          rest.getCustomJavaRepositories(),
          rest.getCustomJavaDeps(),
          rest.getCustomJavaEnvVars(),
          pojaPythonRepositoryName,
          pojaPythonRepositoryDomain,
          pojaDomainOwner,
          POJA_1.getCliVersion());
    }

    public GeneralPojaConf1 toRest() {
      return new GeneralPojaConf1()
          .appName(appName)
          .withSnapstart(withSnapstart)
          .withQueuesNb(withQueuesNb)
          .packageFullName(packageFullName)
          .customJavaDeps(customJavaDeps)
          .customJavaRepositories(customJavaRepositories)
          .customJavaEnvVars(customJavaEnvVars);
    }
  }

  public record GenApiClient(
      @JsonProperty("aws_account_id") String awsAccountId,
      @JsonProperty("with_publish_to_npm_registry") Boolean withPublishToNpmRegistry,
      @JsonProperty("ts_client_default_openapi_server_url") String tsClientDefaultOpenapiServerUrl,
      @JsonProperty("ts_client_api_url_env_var_name") String tsClientApiUrlEnvVarName,
      @JsonProperty("codeartifact_repository_name") String codeartifactRepositoryName,
      @JsonProperty("codeartifact_domain_name") String codeartifactDomainName) {
    @Builder
    public GenApiClient(GenApiClient1 rest) {
      this(
          rest.getAwsAccountId(),
          rest.getWithPublishToNpmRegistry(),
          rest.getTsClientDefaultOpenapiServerUrl(),
          rest.getTsClientApiUrlEnvVarName(),
          rest.getCodeartifactRepositoryName(),
          rest.getCodeartifactDomainName());
    }

    public GenApiClient1 toRest() {
      return new GenApiClient1()
          .awsAccountId(awsAccountId)
          .withPublishToNpmRegistry(withPublishToNpmRegistry)
          .tsClientApiUrlEnvVarName(tsClientApiUrlEnvVarName)
          .tsClientDefaultOpenapiServerUrl(tsClientDefaultOpenapiServerUrl)
          .codeartifactRepositoryName(codeartifactRepositoryName)
          .codeartifactDomainName(codeartifactDomainName);
    }
  }

  public record Integration(
      @JsonProperty("with_sentry") Boolean withSentry,
      @JsonProperty("with_sonar") Boolean withSonar,
      @JsonProperty("with_codeql") Boolean withCodeql,
      @JsonProperty("with_file_storage") Boolean withFileStorage,
      @JsonProperty("with_swagger_ui") Boolean withSwaggerUi) {
    @Builder
    public Integration(Integration1 rest) {
      this(
          rest.getWithSentry(),
          rest.getWithSonar(),
          rest.getWithCodeql(),
          rest.getWithFileStorage(),
          rest.getWithSwaggerUi());
    }

    public Integration1 toRest() {
      return new Integration1()
          .withSentry(withSentry)
          .withSonar(withSonar)
          .withCodeql(withCodeql)
          .withFileStorage(withFileStorage)
          .withSwaggerUi(withSwaggerUi);
    }
  }

  public record TestingConf(
      @JsonProperty("java_facade_it") String javaFacadeIt,
      @JsonProperty("jacoco_min_coverage") BigDecimal jacocoMinCoverage) {
    @Builder
    public TestingConf(api.jcloudify.app.endpoint.rest.model.TestingConf1 rest) {
      this(rest.getJavaFacadeIt(), rest.getJacocoMinCoverage());
    }

    public TestingConf1 toRest() {
      return new TestingConf1().jacocoMinCoverage(jacocoMinCoverage).javaFacadeIt(javaFacadeIt);
    }
  }

  public record MailingConf(@JsonProperty("ses_source") String sesSource) {
    @Builder
    public MailingConf(MailingConf1 rest) {
      this(rest.getSesSource());
    }

    public MailingConf1 toRest() {
      return new MailingConf1().sesSource(sesSource);
    }
  }

  public record Compute(
      @JsonProperty("frontal_memory") BigDecimal frontalMemory,
      @JsonProperty("frontal_function_timeout") BigDecimal frontalFunctionTimeout,
      @JsonProperty("worker_memory") BigDecimal workerMemory,
      @JsonProperty("worker_batch") BigDecimal workerBatch,
      @JsonProperty("worker_function_1_timeout") BigDecimal workerFunction1Timeout,
      @JsonProperty("worker_function_2_timeout") BigDecimal workerFunction2Timeout) {
    @Builder
    public Compute(ComputeConf1 rest) {
      this(
          rest.getFrontalMemory(),
          rest.getFrontalFunctionTimeout(),
          rest.getWorkerMemory(),
          rest.getWorkerBatch(),
          rest.getWorkerFunction1Timeout(),
          rest.getWorkerFunction2Timeout());
    }

    public ComputeConf1 toRest() {
      return new ComputeConf1()
          .frontalMemory(frontalMemory)
          .frontalFunctionTimeout(frontalFunctionTimeout)
          .workerMemory(workerMemory)
          .workerBatch(workerBatch)
          .workerFunction1Timeout(workerFunction1Timeout)
          .workerFunction2Timeout(workerFunction2Timeout);
    }
  }

  public record Concurrency(
      @JsonProperty("frontal_reserved_concurrent_executions_nb") Integer frontalReservedConcurrency,
      @JsonProperty("worker_reserved_concurrent_executions_nb") Integer workerReservedConcurrency) {
    @Builder
    public Concurrency(ConcurrencyConf1 rest) {
      this(
          rest.getFrontalReservedConcurrentExecutionsNb(),
          rest.getWorkerReservedConcurrentExecutionsNb());
    }

    public ConcurrencyConf1 toRest() {
      return new ConcurrencyConf1()
          .frontalReservedConcurrentExecutionsNb(frontalReservedConcurrency)
          .workerReservedConcurrentExecutionsNb(workerReservedConcurrency);
    }
  }

  public record Database(
      @JsonProperty("with_database") DatabaseConf1.WithDatabaseEnum dbType,
      @JsonProperty("database_non_root_username") String dbNonRootUsername,
      @JsonProperty("database_non_root_password") String dbNonrootPassword,
      @JsonProperty("prod_db_cluster_timeout") BigDecimal prodDbClusterTimeout,
      @JsonProperty("aurora_min_capacity") BigDecimal auroraMinCapacity,
      @JsonProperty("aurora_max_capacity") BigDecimal auroraMaxCapacity,
      @JsonProperty("aurora_scale_point") BigDecimal auroraScalePoint,
      @JsonProperty("aurora_sleep") BigDecimal auroraSleep,
      @JsonProperty("aurora_auto_pause") Boolean auroraAutoPause) {
    @Builder
    public Database(DatabaseConf1 rest) {
      this(
          rest.getWithDatabase(),
          rest.getDatabaseNonRootUsername(),
          rest.getDatabaseNonRootPassword(),
          rest.getProdDbClusterTimeout(),
          rest.getAuroraMinCapacity(),
          rest.getAuroraMaxCapacity(),
          rest.getAuroraScalePoint(),
          rest.getAuroraSleep(),
          rest.getAuroraAutoPause());
    }

    public DatabaseConf1 toRest() {
      return new DatabaseConf1()
          .withDatabase(dbType)
          .databaseNonRootUsername(dbNonRootUsername)
          .databaseNonRootPassword(dbNonrootPassword)
          .prodDbClusterTimeout(prodDbClusterTimeout)
          .auroraMinCapacity(auroraMinCapacity)
          .auroraMaxCapacity(auroraMinCapacity)
          .auroraScalePoint(auroraScalePoint)
          .auroraSleep(auroraSleep)
          .auroraAutoPause(auroraAutoPause);
    }
  }
}
