package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.HEALTHY;
import static api.jcloudify.app.endpoint.rest.model.User.RoleEnum.USER;

import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.endpoint.rest.model.User;
import java.time.Instant;
import java.util.List;

public class TestMocks {
  public static final String JOE_DOE_ID = "joe_doe_id";
  public static final String JOE_DOE_TOKEN = "joe_doe_token";
  public static final String JOE_DOE_EMAIL = "joe@email.com";
  public static final String JOE_DOE_GITHUB_ID = "1234";
  public static final String JOE_DOE_USERNAME = "JoeDoe";
  public static final String JOE_DOE_AVATAR =
      "https://github.com/images/" + JOE_DOE_USERNAME + ".png";
  public static final String JANE_DOE_ID = "jane_doe_id";
  public static final String JANE_DOE_TOKEN = "jane_doe_token";
  public static final String JANE_DOE_EMAIL = "jane@email.com";
  public static final String JANE_DOE_GITHUB_ID = "4321";
  public static final String JANE_DOE_USERNAME = "JaneDoe";
  public static final String JANE_DOE_AVATAR =
      "https://github.com/images/" + JANE_DOE_USERNAME + ".png";
  public static final String POJA_CREATED_STACK_ID = "poja_created_stack_id";
  public static final String POJA_CF_STACK_ID = "poja_cf_stack_id";
  public static final String POJA_APPLICATION_ID = "poja_application_id";
  public static final String POJA_APPLICATION_NAME = "poja-test-app";
  public static final String POJA_APPLICATION_ENVIRONMENT_ID = "poja_application_environment_id";
  public static final String POJA_APPLICATION_GITHUB_REPOSITORY =
      "https://github.com/joeDoe/poja_application";
  public static final Instant POJA_APPLICATION_CREATION_DATETIME =
      Instant.parse("2023-06-18T10:15:30.00Z");
  public static final String EVENT_STACK_ID = "event_stack_1_id";
  public static final String EVENT_STACK_NAME = "poja_app_event_stack";
  public static final String BUCKET_STACK_ID = "bucket_stack_1_id";
  public static final String BUCKET_STACK_NAME = "poja_app_bucket_stack";
  public static final String COMPUTE_PERM_STACK_ID = "compute_perm_stack_1_id";
  public static final String COMPUTE_PERM_STACK_NAME = "poja_app_compute_perm_stack";

  public static User joeDoeUser() {
    return new User()
        .id(JOE_DOE_ID)
        .email(JOE_DOE_EMAIL)
        .username(JOE_DOE_USERNAME)
        .role(USER)
        .firstName("Joe")
        .lastName("Doe")
        .githubId(JOE_DOE_GITHUB_ID)
        .avatar(JOE_DOE_AVATAR);
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

  public static Environment prodEnvironment() {
    return new Environment()
        .id(POJA_APPLICATION_ENVIRONMENT_ID)
        .environmentType(EnvironmentType.PROD)
        .state(HEALTHY);
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
        .name(POJA_APPLICATION_NAME + "-2")
        .userId(JOE_DOE_ID)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false);
  }

  public static Application createdApplication() {
    return new Application()
        .id(POJA_APPLICATION_ID)
        .name(POJA_APPLICATION_NAME + "-2")
        .userId(JOE_DOE_ID)
        .creationDatetime(POJA_APPLICATION_CREATION_DATETIME)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false)
        .environments(List.of());
  }

  public static Application updatedApplication() {
    return new Application()
        .id(POJA_APPLICATION_ID)
        .name(POJA_APPLICATION_NAME)
        .userId(JOE_DOE_ID)
        .creationDatetime(POJA_APPLICATION_CREATION_DATETIME)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(true)
        .environments(List.of(prodEnvironment()));
  }

  public static Stack stackDeploymentInitiated(StackType stackType) {
    return new Stack()
        .id(POJA_CREATED_STACK_ID)
        .name("prod-" + stackType.getValue().toLowerCase().replace("_", "-") + "-poja-test-app")
        .cfStackId(POJA_CF_STACK_ID)
        .stackType(stackType)
        .application(applicationToUpdate())
        .environment(prodEnvironment());
  }
}
