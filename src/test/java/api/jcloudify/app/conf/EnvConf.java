package api.jcloudify.app.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {
  private static final String NETWORKING_CONFIG_STRING_VALUE =
      "{\"region\": \"eu-west-3\",\"with_own_vpc\": true, \"ssm_sg_id\": \"sg-id\","
          + " \"ssm_subnet1_id\": \"subnet-1\", \"ssm_subnet2_id\": \"subnet-2\"}";

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.flyway.locations", () -> "classpath:/db/migration,classpath:/db/testdata");
    registry.add("github.client.id", () -> "dummy");
    registry.add("github.client.secret", () -> "dummy");
    registry.add("github.redirect.uri", () -> "dummy");
    registry.add("github.token.url", () -> "dummy");
    registry.add("apps.envs.networking", () -> NETWORKING_CONFIG_STRING_VALUE);
    registry.add("github.api.baseuri", () -> "https://api.github.com");
  }
}
