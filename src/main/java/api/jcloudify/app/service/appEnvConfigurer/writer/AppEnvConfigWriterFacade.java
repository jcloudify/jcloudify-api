package api.jcloudify.app.service.appEnvConfigurer.writer;

import static api.jcloudify.app.model.PojaVersion.POJA_V13_3_1;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.model.exception.NotImplementedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Primary
@Service
public final class AppEnvConfigWriterFacade extends AbstractAppEnvConfigWriter {
  private final PojaConfV13_3_1Writer v1331Writer;

  AppEnvConfigWriterFacade(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper,
      PojaConfV13_3_1Writer v1331Writer) {
    super(yamlObjectMapper);
    this.v1331Writer = v1331Writer;
  }

  @Override
  public File apply(OneOfPojaConf oneOfPojaConf) {
    var casted = (PojaConf) oneOfPojaConf.getActualInstance();
    if (POJA_V13_3_1.toHumanReadableValue().equals(casted.getVersion())) {
      return v1331Writer.apply(oneOfPojaConf);
    }
    throw new NotImplementedException("not implemented yet");
  }

  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    if (POJA_V13_3_1.toHumanReadableValue().equals(pojaConf.getVersion())) {
      return v1331Writer.writeToTempFile(pojaConf);
    }
    throw new NotImplementedException("not implemented yet");
  }
}
