package api.jcloudify.app.unit.validator;

import static api.jcloudify.app.endpoint.rest.model.DatabaseConf1.WithDatabaseEnum.NONE;
import static api.jcloudify.app.endpoint.rest.model.WithQueuesNbEnum.NUMBER_0;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_NAME;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsBadRequestException;
import static api.jcloudify.app.model.PojaVersion.POJA_1;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import api.jcloudify.app.endpoint.rest.model.ComputeConf1;
import api.jcloudify.app.endpoint.rest.model.ConcurrencyConf1;
import api.jcloudify.app.endpoint.rest.model.DatabaseConf1;
import api.jcloudify.app.endpoint.rest.model.GeneralPojaConf1;
import api.jcloudify.app.endpoint.rest.model.Integration1;
import api.jcloudify.app.endpoint.rest.model.MailingConf1;
import api.jcloudify.app.endpoint.rest.model.PojaConf1;
import api.jcloudify.app.endpoint.rest.model.TestingConf1;
import api.jcloudify.app.service.appEnvConfigurer.mapper.PojaConf1RestValidator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class PojaConf1RestValidatorTest {
  private final PojaConf1RestValidator validator = new PojaConf1RestValidator();

  @Test
  void validator_validate_PojaConf1_ok() {
    PojaConf1 validPojaConf1 =
        new PojaConf1()
            .version(POJA_1.toHumanReadableValue())
            .general(
                new GeneralPojaConf1()
                    .appName(POJA_APPLICATION_NAME)
                    .withSnapstart(true)
                    .packageFullName("com.dummy.app")
                    .withQueuesNb(NUMBER_0)
                    .customJavaDeps(new ArrayList<>())
                    .customJavaRepositories(new ArrayList<>())
                    .customJavaEnvVars(new HashMap<>()))
            .compute(
                new ComputeConf1()
                    .frontalMemory(BigDecimal.valueOf(1024))
                    .frontalFunctionTimeout(BigDecimal.valueOf(30))
                    .workerMemory(BigDecimal.valueOf(512))
                    .workerFunction1Timeout(BigDecimal.valueOf(600))
                    .workerFunction2Timeout(BigDecimal.valueOf(600))
                    .workerBatch(BigDecimal.valueOf(5)))
            .testing(
                new TestingConf1().jacocoMinCoverage(BigDecimal.valueOf(0.0)).javaFacadeIt("dummy"))
            .emailing(new MailingConf1().sesSource("dummy@dummy.com"))
            .database(new DatabaseConf1().withDatabase(NONE))
            .concurrency(new ConcurrencyConf1())
            .integration(
                new Integration1()
                    .withCodeql(false)
                    .withSentry(false)
                    .withSonar(false)
                    .withSwaggerUi(false)
                    .withFileStorage(false));

    assertDoesNotThrow(() -> validator.accept(validPojaConf1));
  }

  @Test
  void validator_validate_PojaConf1_ko() {
    assertThrowsBadRequestException(
        "version is mandatory. "
            + "app_name is mandatory. "
            + "with_snapstart is mandatory. "
            + "package_full_name must include three parts separated by dots. "
            + "queues_nb is mandatory. "
            + "integration is mandatory. "
            + "ses_source must be a valid email address. "
            + "testing is mandatory. "
            + "compute is mandatory. "
            + "database is mandatory. "
            + "concurrency is mandatory. ",
        () ->
            validator.accept(
                new PojaConf1()
                    .general(new GeneralPojaConf1().packageFullName("dummy.dummy"))
                    .emailing(new MailingConf1().sesSource("dummy"))));
  }
}
