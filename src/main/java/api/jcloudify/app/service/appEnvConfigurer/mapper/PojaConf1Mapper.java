package api.jcloudify.app.service.appEnvConfigurer.mapper;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.model.pojaConf.conf1.PojaConf1;
import api.jcloudify.app.service.appEnvConfigurer.NetworkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
final class PojaConf1Mapper extends AbstractAppEnvConfigMapper {
  private final PojaConf1RestValidator validator;

  PojaConf1Mapper(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper,
      NetworkingService networkingService,
      PojaConf1RestValidator validator) {
    super(yamlObjectMapper, networkingService);
    this.validator = validator;
  }

  @SneakyThrows
  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    var casted = (api.jcloudify.app.endpoint.rest.model.PojaConf1) pojaConf;
    validator.accept(casted);
    var domainPojaConf = toDomain(casted);
    File namedTempFile =
        createNamedTempFile("conf-v-" + domainPojaConf.version() + "-" + randomUUID() + ".yml");
    this.yamlObjectMapper.writeValue(namedTempFile, domainPojaConf);
    return namedTempFile;
  }

  public OneOfPojaConf read(File file) {
    api.jcloudify.app.endpoint.rest.model.PojaConf1 pojaConf;
    try {
      var domain = yamlObjectMapper.readValue(file, PojaConf1.class);
      pojaConf = toRest(domain);
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
    return new OneOfPojaConf(pojaConf);
  }

  @Override
  public api.jcloudify.app.model.pojaConf.conf1.PojaConf readAsDomain(File file) {
    try {
      return yamlObjectMapper.readValue(file, PojaConf1.class);
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  @Override
  public File write(OneOfPojaConf oneOfPojaConf) {
    return writeToTempFile(oneOfPojaConf.getPojaConf1());
  }

  private PojaConf1 toDomain(api.jcloudify.app.endpoint.rest.model.PojaConf1 rest) {
    return PojaConf1.builder()
        .general(new PojaConf1.General(rest.getGeneral(), null, null, null))
        .integration(new PojaConf1.Integration(rest.getIntegration()))
        .genApiClient(new PojaConf1.GenApiClient(rest.getGenApiClient()))
        .concurrency(new PojaConf1.Concurrency(rest.getConcurrency()))
        .compute(new PojaConf1.Compute(rest.getCompute()))
        .mailing(new PojaConf1.MailingConf(rest.getEmailing()))
        .testing(new PojaConf1.TestingConf(rest.getTesting()))
        .database(new PojaConf1.Database(rest.getDatabase()))
        .networking(networkingService.getNetworkingConfig())
        .build();
  }

  private api.jcloudify.app.endpoint.rest.model.PojaConf1 toRest(PojaConf1 domain) {
    return new api.jcloudify.app.endpoint.rest.model.PojaConf1()
        .general(domain.general().toRest())
        .integration(domain.integration().toRest())
        .genApiClient(domain.genApiClient().toRest())
        .concurrency(domain.concurrency().toRest())
        .compute(domain.compute().toRest())
        .emailing(domain.mailing().toRest())
        .testing(domain.testing().toRest())
        .database(domain.database().toRest())
        .version(domain.version());
  }
}
