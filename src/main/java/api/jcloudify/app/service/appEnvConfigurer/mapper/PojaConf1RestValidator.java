package api.jcloudify.app.service.appEnvConfigurer.mapper;

import api.jcloudify.app.endpoint.rest.model.PojaConf1;
import api.jcloudify.app.model.exception.BadRequestException;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PojaConf1RestValidator implements Consumer<PojaConf1> {

  @Override
  public void accept(PojaConf1 pojaConf1) {
    StringBuilder exceptionMessageBuilder = new StringBuilder();

    if (pojaConf1.getVersion() == null) {
      exceptionMessageBuilder.append("version is mandatory. ");
    }
    if (pojaConf1.getGeneral() == null) {
      exceptionMessageBuilder.append("general is mandatory. ");
    } else {
      if (pojaConf1.getGeneral().getAppName() == null) {
        exceptionMessageBuilder.append("app_name is mandatory. ");
      }
      if (pojaConf1.getGeneral().getWithSnapstart() == null) {
        exceptionMessageBuilder.append("with_snapstart is mandatory. ");
      }
      if (pojaConf1.getGeneral().getPackageFullName() == null) {
        exceptionMessageBuilder.append("package_full_name is mandatory. ");
      }
      if (pojaConf1.getGeneral().getWithQueuesNb() == null) {
        exceptionMessageBuilder.append("queues_nb is mandatory. ");
      }
      if (pojaConf1.getGeneral().getCustomJavaDeps() == null) {
        exceptionMessageBuilder.append("custom_java_deps is mandatory. ");
      }
      if (pojaConf1.getGeneral().getCustomJavaEnvVars() == null) {
        exceptionMessageBuilder.append("custom_java_env_vars is mandatory. ");
      }
      if (pojaConf1.getGeneral().getCustomJavaRepositories() == null) {
        exceptionMessageBuilder.append("custom_java_repositories is mandatory. ");
      }
      if (!isPackageFullNameValid(pojaConf1.getGeneral().getPackageFullName())) {
        exceptionMessageBuilder.append(
            "package_full_name must include three parts separated by dots. ");
      }
    }
    if (pojaConf1.getIntegration() == null) {
      exceptionMessageBuilder.append("integration is mandatory. ");
    } else {
      if (pojaConf1.getIntegration().getWithSwaggerUi() == null) {
        exceptionMessageBuilder.append("with_swagger_ui is mandatory. ");
      }
      if (pojaConf1.getIntegration().getWithCodeql() == null) {
        exceptionMessageBuilder.append("with_codeql is mandatory. ");
      }
      if (pojaConf1.getIntegration().getWithFileStorage() == null) {
        exceptionMessageBuilder.append("with_file_storage is mandatory. ");
      }
      if (pojaConf1.getIntegration().getWithSentry() == null) {
        exceptionMessageBuilder.append("with_sentry is mandatory. ");
      }
      if (pojaConf1.getIntegration().getWithSonar() == null) {
        exceptionMessageBuilder.append("with_sonar is mandatory. ");
      }
    }
    if (pojaConf1.getEmailing() == null) {
      exceptionMessageBuilder.append("emailing is mandatory. ");
    } else {
      if (pojaConf1.getEmailing().getSesSource() == null) {
        exceptionMessageBuilder.append("ses_source is mandatory. ");
      }
      if (!isAValidEmail(pojaConf1.getEmailing().getSesSource())) {
        exceptionMessageBuilder.append("ses_source must be a valid email address. ");
      }
    }
    if (pojaConf1.getTesting() == null) {
      exceptionMessageBuilder.append("testing is mandatory. ");
    } else {
      if (pojaConf1.getTesting().getJacocoMinCoverage() == null) {
        exceptionMessageBuilder.append("jacoco_min_coverage is mandatory. ");
      }
      if (pojaConf1.getTesting().getJavaFacadeIt() == null) {
        exceptionMessageBuilder.append("java_facade_it is mandatory. ");
      }
    }
    if (pojaConf1.getCompute() == null) {
      exceptionMessageBuilder.append("compute is mandatory. ");
    } else {
      if (pojaConf1.getCompute().getFrontalMemory() == null) {
        exceptionMessageBuilder.append("compute_frontal_memory is mandatory. ");
      }
      if (pojaConf1.getCompute().getFrontalFunctionTimeout() == null) {
        exceptionMessageBuilder.append("frontal_function_timeout is mandatory. ");
      }
      if (pojaConf1.getCompute().getWorkerMemory() == null) {
        exceptionMessageBuilder.append("worker_memory is mandatory. ");
      }
      if (pojaConf1.getCompute().getWorkerFunction1Timeout() == null) {
        exceptionMessageBuilder.append("worker_function_1_timeout is mandatory. ");
      }
      if (pojaConf1.getCompute().getWorkerFunction2Timeout() == null) {
        exceptionMessageBuilder.append("worker_function_2_timeout is mandatory. ");
      }
      if (pojaConf1.getCompute().getWorkerBatch() == null) {
        exceptionMessageBuilder.append("worker_batch is mandatory. ");
      }
    }
    if (pojaConf1.getDatabase() == null) {
      exceptionMessageBuilder.append("database is mandatory. ");
    } else {
      if (pojaConf1.getDatabase().getWithDatabase() == null) {
        exceptionMessageBuilder.append("with_database is mandatory. ");
      }
    }
    if (pojaConf1.getConcurrency() == null) {
      exceptionMessageBuilder.append("concurrency is mandatory. ");
    }
    String exceptionMessage = exceptionMessageBuilder.toString();
    if (!exceptionMessage.isEmpty()) {
      throw new BadRequestException(exceptionMessage);
    }
  }

  private boolean isPackageFullNameValid(String packageFullName) {
    return List.of(packageFullName.split("\\.")).size() == 3;
  }

  private boolean isAValidEmail(String email) {
    String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    return Pattern.compile(EMAIL_REGEX_PATTERN).matcher(email).matches();
  }
}
