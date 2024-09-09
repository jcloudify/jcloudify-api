package api.jcloudify.app.endpoint.event.model;

import static api.jcloudify.app.endpoint.event.EventStack.EVENT_STACK_2;
import static api.jcloudify.app.endpoint.event.utils.TestMocks.MOCK_BUCKET_KEY;
import static api.jcloudify.app.endpoint.event.utils.TestMocks.MOCK_INSTANT;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.service.pricing.PricingMethod.TEN_MICRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EventModelSerializationIT extends MockedThirdParties {
  private static @NotNull RefreshUsersBillingInfoTriggered refreshUsersBillingInfoTriggered() {
    return new RefreshUsersBillingInfoTriggered();
  }

  private static @NotNull RefreshUserBillingInfoRequested refreshUserBillingInfoRequested() {
    return new RefreshUserBillingInfoRequested(
        JOE_DOE_ID, refreshUsersBillingInfoTriggered(), TEN_MICRO);
  }

  private RefreshAppBillingInfoRequested refreshAppBillingInfoRequested() {
    return new RefreshAppBillingInfoRequested(
        JOE_DOE_ID, POJA_APPLICATION_ID, refreshUserBillingInfoRequested());
  }

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

  @Test
  void refresh_users_billing_info_triggered_serialization() {
    var event = refreshUsersBillingInfoTriggered();

    assertNotNull(event.getId());
    assertNotNull(event.getNow());
    assertNotNull(event.getUtcLocalDate());
    assertNotNull(event.getUtcStartOfDay());
    assertEquals(Duration.ofMinutes(5), event.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), event.maxConsumerBackoffBetweenRetries());
  }

  @Test
  void refresh_user_billing_info_requested_serialization() {
    var event = refreshUserBillingInfoRequested();

    assertNotNull(event.getId());
    assertNotNull(event.getPricingCalculationRequestStartTime());
    assertNotNull(event.getPricingCalculationRequestEndTime());
    assertNotNull(event.getPricingMethod());
    assertNotNull(event.getRefreshUsersBillingInfoTriggered());
    assertNotNull(event.getUserId());
    assertEquals(Duration.ofMinutes(5), event.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), event.maxConsumerBackoffBetweenRetries());
  }

  @Test
  void refresh_app_billing_info_requested_serialization() {
    var event = refreshAppBillingInfoRequested();

    assertNotNull(event.getPricingCalculationRequestStartTime());
    assertNotNull(event.getPricingCalculationRequestEndTime());
    assertNotNull(event.getPricingMethod());
    assertEquals(Duration.ofMinutes(5), event.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), event.maxConsumerBackoffBetweenRetries());
  }

  @SneakyThrows
  @Test
  void refresh_env_billing_info_requested_serialization() {
    var event =
        new RefreshEnvBillingInfoRequested(
            POJA_APPLICATION_ENVIRONMENT_ID,
            JOE_DOE_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            refreshAppBillingInfoRequested());

    String s = om.writeValueAsString(event);
    var dese = om.readValue(s, RefreshEnvBillingInfoRequested.class);

    assertNotNull(event.getPricingCalculationRequestStartTime());
    assertNotNull(event.getPricingCalculationRequestEndTime());
    assertNotNull(event.getPricingMethod());
    assertEquals(EVENT_STACK_2, event.getEventStack());
    assertEquals(Duration.ofMinutes(10), event.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), event.maxConsumerBackoffBetweenRetries());
  }

  @SneakyThrows
  @Test
  void get_billing_info_query_result_requested_serialization() {
    var event = new GetBillingInfoQueryResultRequested("queryId");

    String s = om.writeValueAsString(event);
    GetBillingInfoQueryResultRequested dese =
        om.readValue(s, GetBillingInfoQueryResultRequested.class);

    assertNotNull(dese);
    assertEquals(Duration.ofSeconds(30), event.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), event.maxConsumerBackoffBetweenRetries());
  }
}
