package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.endpoint.rest.model.User.RoleEnum.USER;

import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplication;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
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
  public static final String POJA_CREATED_STACK_ID = "poja_created_stack_id";
  public static final String POJA_CF_STACK_ID = "poja_cf_stack_id";
  public static final String POJA_APPLICATION_ID = "poja_application_id";
  public static final String POJA_APPLICATION_ENVIRONMENT_ID = "poja_application_environment_id";

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

  public static Environment prodEnvironment() {
    return new Environment()
            .id(POJA_APPLICATION_ENVIRONMENT_ID)
            .environmentType(EnvironmentType.PROD)
            .state(Environment.StateEnum.HEALTHY);
  }

  public static CrupdateApplication applicationToCreate() {
    return new CrupdateApplication()
            .id(POJA_APPLICATION_ID)
            .name("poja_application_name")
            .userId(JOE_DOE_ID)
            .archived(false);
  }

  public static Application createdApplication() {
    return new Application()
            .id(POJA_APPLICATION_ID)
            .name("poja_application_name")
            .userId(JOE_DOE_ID)
            .creationDatetime(Instant.parse("2023-06-18T10:15:30.00Z"))
            .githubRepository("https://github.com/joeDoe/poja_application")
            .archived(false)
            .environments(List.of(prodEnvironment()));
  }
}
