package api.jcloudify.app.repository.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnvironmentTest {
  @Test
  void get_latest_conf_ok() {
    var env = getEnvironment();

    assertEquals(conf3, env.getLatestDeploymentConf());
  }

  private static Environment getEnvironment() {
    Environment environment = new Environment();

    environment.setEnvDeploymentConfs(List.of(conf1, conf2, conf3));
    return environment;
  }

  private static EnvDeploymentConf conf1 =
      EnvDeploymentConf.builder()
          .id("id_1")
          .creationDatetime(Instant.parse("2023-06-18T10:15:30.00Z"))
          .build();
  private static EnvDeploymentConf conf2 =
      EnvDeploymentConf.builder()
          .id("id_2")
          .creationDatetime(Instant.parse("2023-06-18T11:15:30.00Z"))
          .build();
  private static EnvDeploymentConf conf3 =
      EnvDeploymentConf.builder()
          .creationDatetime(Instant.parse("2023-06-18T12:15:30.00Z"))
          .id("id_3")
          .build();
}
