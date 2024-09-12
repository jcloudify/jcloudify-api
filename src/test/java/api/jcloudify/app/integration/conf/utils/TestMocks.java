package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.endpoint.rest.model.DatabaseConf1.WithDatabaseEnum.NONE;
import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.HEALTHY;
import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.UNKNOWN;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.CREATE_COMPLETE;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.CREATE_IN_PROGRESS;
import static api.jcloudify.app.endpoint.rest.model.StackResourceStatusType.UPDATE_IN_PROGRESS;
import static api.jcloudify.app.endpoint.rest.model.User.RoleEnum.USER;
import static api.jcloudify.app.endpoint.rest.model.WithQueuesNbEnum.NUMBER_2;
import static api.jcloudify.app.model.PojaVersion.POJA_1;

import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.endpoint.rest.model.ComputeConf1;
import api.jcloudify.app.endpoint.rest.model.ConcurrencyConf1;
import api.jcloudify.app.endpoint.rest.model.CreateSsmParameter;
import api.jcloudify.app.endpoint.rest.model.DatabaseConf1;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.GenApiClient1;
import api.jcloudify.app.endpoint.rest.model.GeneralPojaConf1;
import api.jcloudify.app.endpoint.rest.model.GithubRepository;
import api.jcloudify.app.endpoint.rest.model.Integration1;
import api.jcloudify.app.endpoint.rest.model.LogGroup;
import api.jcloudify.app.endpoint.rest.model.LogStream;
import api.jcloudify.app.endpoint.rest.model.LogStreamEvent;
import api.jcloudify.app.endpoint.rest.model.MailingConf1;
import api.jcloudify.app.endpoint.rest.model.PojaConf1;
import api.jcloudify.app.endpoint.rest.model.SsmParameter;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.endpoint.rest.model.StackOutput;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.endpoint.rest.model.TestingConf1;
import api.jcloudify.app.endpoint.rest.model.User;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.ssm.model.Parameter;

public class TestMocks {
  public static final String JOE_DOE_ID = "joe_doe_id";
  public static final String JOE_DOE_TOKEN = "joe_doe_token";
  public static final String A_GITHUB_APP_TOKEN = "github_app_token";
  public static final String JOE_DOE_EMAIL = "joe@email.com";
  public static final String JOE_DOE_GITHUB_ID = "1234";
  public static final String JOE_DOE_USERNAME = "JoeDoe";
  public static final String JOE_DOE_AVATAR =
      "https://github.com/images/" + JOE_DOE_USERNAME + ".png";
  public static final String JOE_DOE_STRIPE_ID = "joe_stripe_id";
  public static final String JANE_DOE_ID = "jane_doe_id";
  public static final String JANE_DOE_TOKEN = "jane_doe_token";
  public static final String JANE_DOE_EMAIL = "jane@email.com";
  public static final String JANE_DOE_GITHUB_ID = "4321";
  public static final String JANE_DOE_USERNAME = "JaneDoe";
  public static final String JANE_DOE_AVATAR =
      "https://github.com/images/" + JANE_DOE_USERNAME + ".png";
  public static final String JANE_DOE_STRIPE_ID = "jane_stripe_id";
  public static final String POJA_CREATED_STACK_ID = "poja_created_stack_id";
  public static final String POJA_CF_STACK_ID = "poja_cf_stack_id";
  public static final String POJA_APPLICATION_ID = "poja_application_id";
  public static final String POJA_APPLICATION_NAME = "poja-test-app";
  public static final String POJA_APPLICATION_ENVIRONMENT_ID = "poja_application_environment_id";
  public static final String GH_APP_INSTALL_1_ID = "gh_app_install_1_id";
  public static final GithubRepository POJA_APPLICATION_GITHUB_REPOSITORY =
      new GithubRepository()
          .name("poja_application")
          .isPrivate(false)
          .description("a regular poja app")
          .installationId(GH_APP_INSTALL_1_ID);
  public static final Instant POJA_APPLICATION_CREATION_DATETIME =
      Instant.parse("2023-06-18T10:15:30.00Z");
  public static final String EVENT_STACK_ID = "event_stack_1_id";
  public static final String EVENT_STACK_NAME = "poja_app_event_stack";
  public static final String BUCKET_STACK_ID = "bucket_stack_1_id";
  public static final String BUCKET_STACK_NAME = "poja_app_bucket_stack";
  public static final String COMPUTE_PERM_STACK_ID = "compute_perm_stack_1_id";
  public static final String COMPUTE_PERM_STACK_NAME = "poja_app_compute_perm_stack";
  public static final String OTHER_POJA_APPLICATION_ID = "other_poja_application_id";
  public static final String OTHER_POJA_APPLICATION_ENVIRONMENT_ID =
      "other_poja_application_environment_id";
  public static final String OTHER_POJA_APPLICATION_ENVIRONMENT_2_ID =
      "other_poja_application_environment_2_id";
  public static final String PROD_COMPUTE_FRONTAL_FUNCTION = "prod-compute-frontal-function";
  public static final String PROD_COMPUTE_WORKER_1_FUNCTION = "prod-compute-worker-1-function";
  public static final String PROD_COMPUTE_WORKER_2_FUNCTION = "prod-compute-worker-2-function";
  public static final String PROD_COMPUTE_FRONTAL_FUNCTION_LOG_GROUP =
      "/aws/lambda/prod-compute-frontal-function";
  public static final String FIRST_LOG_STREAM_NAME = "2024/01/01/[$LATEST]12345";
  public static final String SECOND_LOG_STREAM_NAME = "2024/01/01/[$LATEST]67891";
  public static final String THIRD_LOG_STREAM_NAME = "2024/01/01/[$LATEST]011121";
  public static final String POJA_APPLICATION_2_ID = "poja_application_2_id";

  public static Customer stripeCustomer() {
    Customer customer = new Customer();
    customer.setId(JOE_DOE_STRIPE_ID);
    customer.setName("stripe customer");
    customer.setEmail("test@example.com");
    return customer;
  }

  public static PaymentMethod paymentMethod() {
    PaymentMethod.Card card = new PaymentMethod.Card();
    card.setBrand("visa");
    card.setLast4("4242");

    PaymentMethod paymentMethod = new PaymentMethod();
    paymentMethod.setId("payment_method_id");
    paymentMethod.setType("card");
    paymentMethod.setCard(card);
    paymentMethod.setCustomer(JOE_DOE_STRIPE_ID);
    return paymentMethod;
  }

  public static List<PaymentMethod> paymentMethods() {
    List<PaymentMethod> paymentMethods = new ArrayList<>();
    paymentMethods.add(paymentMethod());
    return paymentMethods;
  }

  public static User joeDoeUser() {
    return new User()
        .id(JOE_DOE_ID)
        .email(JOE_DOE_EMAIL)
        .username(JOE_DOE_USERNAME)
        .role(USER)
        .firstName("Joe")
        .lastName("Doe")
        .githubId(JOE_DOE_GITHUB_ID)
        .avatar(JOE_DOE_AVATAR)
        .stripeId(JOE_DOE_STRIPE_ID);
  }

  public static User janeDoeUser() {
    return new User()
        .id(JANE_DOE_ID)
        .email(JANE_DOE_EMAIL)
        .username(JANE_DOE_USERNAME)
        .role(USER)
        .firstName("Jane")
        .lastName("Doe")
        .githubId(JANE_DOE_GITHUB_ID)
        .avatar(JANE_DOE_AVATAR);
  }

  public static Environment pojaAppProdEnvironment() {
    return new Environment()
        .id(POJA_APPLICATION_ENVIRONMENT_ID)
        .environmentType(PROD)
        .state(HEALTHY);
  }

  public static Environment pojaAppPreprodEnvironment() {
    return new Environment()
        .id("poja_preprod_application_environment_id")
        .environmentType(PREPROD)
        .state(UNKNOWN);
  }

  public static ApplicationBase applicationToUpdate() {
    return new ApplicationBase()
        .id(POJA_APPLICATION_ID)
        .name(POJA_APPLICATION_NAME)
        .userId(JOE_DOE_ID)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false);
  }

  public static ApplicationBase applicationToCreate() {
    return new ApplicationBase()
        .id(POJA_APPLICATION_ID + "_2")
        .name(POJA_APPLICATION_NAME + "-2")
        .userId(JOE_DOE_ID)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false);
  }

  public static Application joePojaApplication1() {
    return new Application()
        .id(POJA_APPLICATION_ID)
        .name(POJA_APPLICATION_NAME)
        .userId(JOE_DOE_ID)
        .creationDatetime(POJA_APPLICATION_CREATION_DATETIME)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false)
        .environments(List.of(pojaAppProdEnvironment()))
        .repositoryUrl("http://github.com/user/repo");
  }

  public static Application joePojaApplication2() {
    return new Application()
        .id(POJA_APPLICATION_2_ID)
        .name("poja-test-app-2")
        .userId(JOE_DOE_ID)
        .creationDatetime(Instant.parse("2023-06-18T10:16:30.00Z"))
        .githubRepository(
            new GithubRepository()
                .name("poja_application_2")
                .isPrivate(false)
                .description("a regular poja app")
                .installationId(GH_APP_INSTALL_1_ID))
        .archived(false)
        .environments(List.of())
        .repositoryUrl("http://github.com/user/repo");
  }

  public static Application janePojaApplication() {
    return new Application()
        .id("poja_application_3_id")
        .name("poja-test-app-3")
        .userId(JANE_DOE_ID)
        .creationDatetime(Instant.parse("2023-06-18T10:17:30.00Z"))
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false)
        .environments(List.of())
        .repositoryUrl("http://github.com/user/repo");
  }

  public static Stack stackDeploymentInitiated(StackType stackType) {
    return new Stack()
        .id(POJA_CREATED_STACK_ID)
        .name("prod-" + stackType.getValue().toLowerCase().replace("_", "-") + "-poja-test-app")
        .cfStackId(POJA_CF_STACK_ID)
        .stackType(stackType)
        .application(applicationToUpdate())
        .environment(pojaAppProdEnvironment());
  }

  public static PojaConf1 getValidPojaConf1() {
    String humanReadableValuePojaVersion = POJA_1.toHumanReadableValue();
    return new PojaConf1()
        .version(humanReadableValuePojaVersion)
        .general(
            new GeneralPojaConf1()
                .appName("appname")
                .packageFullName("com.test.api")
                .withSnapstart(false)
                .withQueuesNb(NUMBER_2)
                .customJavaDeps(List.of())
                .customJavaEnvVars(Map.of())
                .customJavaRepositories(List.of()))
        .database(
            new DatabaseConf1()
                .withDatabase(NONE)
                .databaseNonRootPassword(null)
                .databaseNonRootUsername(null)
                .prodDbClusterTimeout(null)
                .auroraAutoPause(null)
                .auroraMaxCapacity(null)
                .auroraMinCapacity(null)
                .auroraSleep(null)
                .auroraScalePoint(null))
        .emailing(new MailingConf1().sesSource("mail@mail.com"))
        .genApiClient(
            new GenApiClient1()
                .awsAccountId(null)
                .tsClientDefaultOpenapiServerUrl(null)
                .tsClientApiUrlEnvVarName(null)
                .codeartifactRepositoryName(null)
                .codeartifactDomainName(null))
        .integration(
            new Integration1()
                .withSentry(false)
                .withSwaggerUi(false)
                .withSonar(false)
                .withFileStorage(false)
                .withCodeql(false))
        .compute(
            new ComputeConf1()
                .frontalMemory(BigDecimal.valueOf(1024))
                .frontalFunctionTimeout(BigDecimal.valueOf(600))
                .workerMemory(BigDecimal.valueOf(512))
                .workerBatch(BigDecimal.valueOf(5))
                .workerFunction1Timeout(BigDecimal.valueOf(600))
                .workerFunction2Timeout(BigDecimal.valueOf(600)))
        .concurrency(
            new ConcurrencyConf1()
                .frontalReservedConcurrentExecutionsNb(null)
                .workerReservedConcurrentExecutionsNb(null))
        .testing(
            new TestingConf1().jacocoMinCoverage(BigDecimal.valueOf(0.2)).javaFacadeIt("FacadeIT"));
  }

  public static List<StackEvent> permStackEvents() {
    StackEvent createInProgress =
        new StackEvent()
            .eventId("ExecutionRole-CREATE_IN_PROGRESS-2024-07-26T05:08:30.029Z")
            .logicalResourceId("ExecutionRole")
            .resourceType("AWS::IAM::Role")
            .timestamp(Instant.parse("2024-07-26T05:08:30.029Z"))
            .resourceStatus(CREATE_IN_PROGRESS)
            .statusMessage(null);
    StackEvent createComplete =
        new StackEvent()
            .eventId("ExecutionRole-CREATE_COMPLETE-2024-07-26T05:08:48.624Z")
            .logicalResourceId("ExecutionRole")
            .resourceType("AWS::IAM::Role")
            .timestamp(Instant.parse("2024-07-26T05:08:48.624Z"))
            .resourceStatus(CREATE_COMPLETE)
            .statusMessage(null);
    StackEvent updateInProgress =
        new StackEvent()
            .eventId("9094a550-4b12-11ef-804a-0642aee31ca5")
            .logicalResourceId("prod-compute-permission-poja-second")
            .resourceType("AWS::CloudFormation::Stack")
            .timestamp(Instant.parse("2024-07-26T05:47:37.873Z"))
            .resourceStatus(UPDATE_IN_PROGRESS)
            .statusMessage("User Initiated");
    return List.of(createInProgress, createComplete, updateInProgress);
  }

  public static List<StackOutput> eventStackOutputs() {
    StackOutput eventBridgeBusName =
        new StackOutput()
            .description(null)
            .key("/other-poja-test-app/prod/eventbridge/bus-name")
            .value("dummy");
    StackOutput eventBridgeArnName =
        new StackOutput()
            .description(null)
            .key("/other-poja-test-app/prod/eventbridge/bus-arn")
            .value("dummy-arn");
    return List.of(eventBridgeBusName, eventBridgeArnName);
  }

  public static SsmParameter ssmParameter(String id, String name, String value) {
    return new SsmParameter().id(id).name(name).value(value);
  }

  public static Parameter awsSsmParameterModelToCreate() {
    return Parameter.builder().name("/poja/prod/ssm/new/param").value("dummy").build();
  }

  public static Parameter awsSsmParameterModelToUpdate() {
    return Parameter.builder().name("/poja/prod/ssm/param1").value("param1").build();
  }

  public static CreateSsmParameter ssmParameterToCreate() {
    return new CreateSsmParameter().name("/poja/prod/ssm/new/param").value("dummy");
  }

  public static SsmParameter ssmParamCreated() {
    return ssmParameter(null, "/poja/prod/ssm/new/param", "dummy");
  }

  public static SsmParameter ssmParam1Updated() {
    return ssmParameter("ssm_param_1_id", "/poja/prod/ssm/param1", "param1");
  }

  public static Parameter ssmParameter(String name, String value) {
    return Parameter.builder().name(name).value(value).build();
  }

  public static LogGroup prodComputeFrontalFunctionLogGroup() {
    return new LogGroup()
        .name(PROD_COMPUTE_FRONTAL_FUNCTION_LOG_GROUP)
        .creationDatetime(Instant.parse("2024-09-02T16:16:21.593Z"));
  }

  public static List<LogStream> prodLogGroupLogStreams() {
    LogStream firstLogStream =
        new LogStream()
            .name(FIRST_LOG_STREAM_NAME)
            .creationDatetime(Instant.parse("2024-01-01T00:00:01.000Z"))
            .firstEventDatetime(Instant.parse("2024-01-01T00:00:01.100Z"))
            .lastEventDatetime(Instant.parse("2024-01-01T00:13:01.000Z"));
    LogStream secondLogStream =
        new LogStream()
            .name(SECOND_LOG_STREAM_NAME)
            .creationDatetime(Instant.parse("2024-01-01T02:00:01.000Z"))
            .firstEventDatetime(Instant.parse("2024-01-01T02:00:01.100Z"))
            .lastEventDatetime(Instant.parse("2024-01-01T02:13:01.000Z"));
    LogStream thirdLogStream =
        new LogStream()
            .name(THIRD_LOG_STREAM_NAME)
            .creationDatetime(Instant.parse("2024-01-01T01:00:01.000Z"))
            .firstEventDatetime(Instant.parse("2024-01-01T01:00:01.100Z"))
            .lastEventDatetime(Instant.parse("2024-01-01T01:13:01.000Z"));
    return List.of(firstLogStream, secondLogStream, thirdLogStream);
  }

  public static List<LogStreamEvent> prodFirstLogStreamEvents() {
    LogStreamEvent initEvent =
        new LogStreamEvent()
            .message(
                "INIT_START Runtime Version: java:21.v20 Runtime Version ARN:"
                    + " arn:aws:lambda:eu-west-3::runtime:1234")
            .timestamp(Instant.parse("2024-09-04T13:23:12.486Z"));
    LogStreamEvent startEvent =
        new LogStreamEvent()
            .message("START RequestId: 628c434b-f53f-4c67-9202-79078bbcd894 Version: 34")
            .timestamp(Instant.parse("2024-01-10T15:30:52.254Z"));
    return List.of(initEvent, startEvent);
  }
}
