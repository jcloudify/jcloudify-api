package api.jcloudify.app.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {
  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.flyway.locations", () -> "classpath:/db/migration,classpath:/db/testdata");
    registry.add("github.client.id", () -> "dummy");
    registry.add("github.client.secret", () -> "dummy");
    registry.add("github.redirect.uri", () -> "dummy");
    registry.add("github.token.url", () -> "dummy");
    registry.add("event.stack.url", () -> "dummy");
    registry.add("compute.permission.stack.url", () -> "dummy");
    registry.add("storage.bucket.stack.url", () -> "dummy");
    registry.add("storage.database.stack.url", () -> "dummy");
  }
}
