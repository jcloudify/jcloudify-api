package api.jcloudify.app.service.appEnvConfigurer.mapper;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import java.io.File;

public interface PojaConfFileMapper {
  OneOfPojaConf read(File file);

  File write(OneOfPojaConf oneOfPojaConf);
}
