package api.jcloudify.app.integration.conf.utils;

import api.jcloudify.app.endpoint.rest.model.Plan;
import api.jcloudify.app.endpoint.rest.model.User;
import java.math.BigDecimal;

import static api.jcloudify.app.endpoint.rest.model.User.RoleEnum.USER;

public class TestMocks {
  public static final String JOE_DOE_ID = "joe_doe_id";
  public static final String JOE_DOE_TOKEN = "joe_doe_token";
  public static final String JOE_DOE_EMAIL = "joe@email.com";
  public static final String FREE_PLAN_ID = "plan_1_id";

  public static User joeDoeUser() {
    return new User()
        .id(JOE_DOE_ID)
        .email(JOE_DOE_EMAIL)
        .username("JoeDoe")
        .role(USER)
        .firstName("Joe")
        .lastName("Doe")
        .githubId("1234")
        .plan(freePlan());
  }

  public static Plan freePlan() {
    return new Plan().id(FREE_PLAN_ID).name("free").cost(0.0);
  }
}
