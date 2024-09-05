package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.event.utils.TestMocks.MOCK_BUCKET_KEY;
import static api.jcloudify.app.endpoint.event.utils.TestMocks.MOCK_INSTANT;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EventModelSerializationIT extends MockedThirdParties {
  @Autowired ObjectMapper om;

  private static BuiltEnvInfo builtEnvInfo() {
    return new BuiltEnvInfo()
        .id("build_env_info_id")
        .formattedBucketKey(MOCK_BUCKET_KEY)
        .environmentType(PROD);
  }

  @Test
  void app_env_compute_deploy_requested_serialization() throws JsonProcessingException {
    var appEnvComputeRequested =
        new AppEnvComputeDeployRequested(
            JOE_DOE_ID,
            POJA_APPLICATION_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            MOCK_BUCKET_KEY,
            "mock_app_name",
            PROD,
            MOCK_INSTANT);

    var serialized = om.writeValueAsString(appEnvComputeRequested);
    var deserialized = om.readValue(serialized, AppEnvComputeDeployRequested.class);

    assertEquals(appEnvComputeRequested, deserialized);
    assertEquals(MOCK_BUCKET_KEY, deserialized.getFormattedBucketKey());
    assertEquals(Duration.ofSeconds(50), deserialized.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), deserialized.maxConsumerBackoffBetweenRetries());
  }

  @Test
  void check_template_integrity_triggered_serialization() throws JsonProcessingException {
    var checkTemplateIntegrityTriggered =
        new CheckTemplateIntegrityTriggered(
            JOE_DOE_ID,
            POJA_APPLICATION_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            MOCK_BUCKET_KEY,
            MOCK_BUCKET_KEY,
            builtEnvInfo(),
            "deployment_conf_id");

    var serialized = om.writeValueAsString(checkTemplateIntegrityTriggered);
    var deserialized = om.readValue(serialized, CheckTemplateIntegrityTriggered.class);

    assertEquals(checkTemplateIntegrityTriggered, deserialized);
    assertEquals(MOCK_BUCKET_KEY, deserialized.getBuiltProjectBucketKey());
    assertEquals(builtEnvInfo(), deserialized.getBuiltEnvInfo());
    assertEquals(Duration.ofSeconds(50), deserialized.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), deserialized.maxConsumerBackoffBetweenRetries());
  }
}
