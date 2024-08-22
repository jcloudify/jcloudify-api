package api.jcloudify.app.service.appEnvConfigurer.mapper;

import api.jcloudify.app.endpoint.rest.model.ComputeConf1;
import api.jcloudify.app.endpoint.rest.model.ConcurrencyConf1;
import api.jcloudify.app.endpoint.rest.model.DatabaseConf1;
import api.jcloudify.app.endpoint.rest.model.GeneralPojaConf1;
import api.jcloudify.app.endpoint.rest.model.Integration1;
import api.jcloudify.app.endpoint.rest.model.MailingConf1;
import api.jcloudify.app.endpoint.rest.model.PojaConf1;
import api.jcloudify.app.endpoint.rest.model.TestingConf1;
import api.jcloudify.app.model.exception.BadRequestException;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PojaConf1RestValidator implements Consumer<PojaConf1> {

  @Override
  public void accept(PojaConf1 pojaConf1) {
    StringBuilder exceptionMessageBuilder = new StringBuilder();

    GeneralPojaConf1 generalConf = pojaConf1.getGeneral();
    Integration1 integrationConf = pojaConf1.getIntegration();
    MailingConf1 mailingConf = pojaConf1.getEmailing();
    TestingConf1 testingConf = pojaConf1.getTesting();
    ComputeConf1 computeConf = pojaConf1.getCompute();
    DatabaseConf1 databaseConf = pojaConf1.getDatabase();
    ConcurrencyConf1 concurrencyConf = pojaConf1.getConcurrency();

    if (generalConf == null) {
      exceptionMessageBuilder.append("general is mandatory. ");
    } else {
      if (generalConf.getAppName() == null) {
        exceptionMessageBuilder.append("general.app_name is mandatory. ");
      }
      if (generalConf.getWithSnapstart() == null) {
        exceptionMessageBuilder.append("general.with_snapstart is mandatory. ");
      }
      if (generalConf.getPackageFullName() == null) {
        exceptionMessageBuilder.append("general.package_full_name is mandatory. ");
      } else {
        if (!isPackageFullNameValid(generalConf.getPackageFullName())) {
          exceptionMessageBuilder.append(
              "general.package_full_name must include three parts separated by dots. ");
        }
      }
      if (generalConf.getWithQueuesNb() == null) {
        exceptionMessageBuilder.append("general.queues_nb is mandatory. ");
      }
      if (generalConf.getCustomJavaDeps() == null) {
        exceptionMessageBuilder.append("general.custom_java_deps is mandatory. ");
      }
      if (generalConf.getCustomJavaEnvVars() == null) {
        exceptionMessageBuilder.append("general.custom_java_env_vars is mandatory. ");
      }
      if (generalConf.getCustomJavaRepositories() == null) {
        exceptionMessageBuilder.append("general.custom_java_repositories is mandatory. ");
      }
    }
    if (integrationConf == null) {
      exceptionMessageBuilder.append("integration is mandatory. ");
    } else {
      if (integrationConf.getWithSwaggerUi() == null) {
        exceptionMessageBuilder.append("integration.with_swagger_ui is mandatory. ");
      }
      if (integrationConf.getWithCodeql() == null) {
        exceptionMessageBuilder.append("integration.with_codeql is mandatory. ");
      }
      if (integrationConf.getWithFileStorage() == null) {
        exceptionMessageBuilder.append("integration.with_file_storage is mandatory. ");
      }
      if (integrationConf.getWithSentry() == null) {
        exceptionMessageBuilder.append("integration.with_sentry is mandatory. ");
      }
      if (integrationConf.getWithSonar() == null) {
        exceptionMessageBuilder.append("integration.with_sonar is mandatory. ");
      }
    }
    if (mailingConf == null) {
      exceptionMessageBuilder.append("emailing is mandatory. ");
    } else {
      if (mailingConf.getSesSource() == null) {
        exceptionMessageBuilder.append("emailing.ses_source is mandatory. ");
      } else {
        if (!isAValidEmail(mailingConf.getSesSource())) {
          exceptionMessageBuilder.append("emailing.ses_source must be a valid email address. ");
        }
      }
    }
    if (testingConf == null) {
      exceptionMessageBuilder.append("testing is mandatory. ");
    } else {
      if (testingConf.getJacocoMinCoverage() == null) {
        exceptionMessageBuilder.append("testing.jacoco_min_coverage is mandatory. ");
      }
      if (testingConf.getJavaFacadeIt() == null) {
        exceptionMessageBuilder.append("testing.java_facade_it is mandatory. ");
      }
    }
    if (computeConf == null) {
      exceptionMessageBuilder.append("compute is mandatory. ");
    } else {
      if (computeConf.getFrontalMemory() == null) {
        exceptionMessageBuilder.append("compute.compute_frontal_memory is mandatory. ");
      }
      if (computeConf.getFrontalFunctionTimeout() == null) {
        exceptionMessageBuilder.append("compute.frontal_function_timeout is mandatory. ");
      }
      if (computeConf.getWorkerMemory() == null) {
        exceptionMessageBuilder.append("compute.worker_memory is mandatory. ");
      }
      if (computeConf.getWorkerFunction1Timeout() == null) {
        exceptionMessageBuilder.append("compute.worker_function_1_timeout is mandatory. ");
      }
      if (computeConf.getWorkerFunction2Timeout() == null) {
        exceptionMessageBuilder.append("compute.worker_function_2_timeout is mandatory. ");
      }
      if (computeConf.getWorkerBatch() == null) {
        exceptionMessageBuilder.append("compute.worker_batch is mandatory. ");
      }
    }
    if (databaseConf == null) {
      exceptionMessageBuilder.append("database is mandatory. ");
    } else {
      if (databaseConf.getWithDatabase() == null) {
        exceptionMessageBuilder.append("database.with_database is mandatory. ");
      }
    }
    if (concurrencyConf == null) {
      exceptionMessageBuilder.append("database.concurrency is mandatory. ");
    }
    String exceptionMessage = exceptionMessageBuilder.toString();
    if (!exceptionMessage.isEmpty()) {
      throw new BadRequestException(exceptionMessage);
    }
  }

  private boolean isPackageFullNameValid(String packageFullName) {
    return packageFullName.split("\\.").length == 3;
  }

  private boolean isAValidEmail(String email) {
    String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    return Pattern.compile(EMAIL_REGEX_PATTERN).matcher(email).matches();
  }
}
