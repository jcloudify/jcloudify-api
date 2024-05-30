package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.endpoint.rest.model.User.RoleEnum.USER;

import api.jcloudify.app.endpoint.rest.model.User;

public class TestMocks {
  public static final String JOE_DOE_ID = "joe_doe_id";
  public static final String JOE_DOE_TOKEN = "joe_doe_token";
  public static final String JOE_DOE_EMAIL = "joe@email.com";

  public static User joeDoeUser() {
    return new User()
        .id(JOE_DOE_ID)
        .email(JOE_DOE_EMAIL)
        .username("JoeDoe")
        .role(USER)
        .firstName("Joe")
        .lastName("Doe")
        .githubId("1234");
  }
}
