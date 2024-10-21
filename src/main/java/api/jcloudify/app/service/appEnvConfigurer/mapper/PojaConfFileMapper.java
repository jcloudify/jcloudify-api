package api.jcloudify.app.service.appEnvConfigurer.mapper;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.model.pojaConf.conf1.PojaConf;
import java.io.File;

public interface PojaConfFileMapper {
  OneOfPojaConf read(File file);

  PojaConf readAsDomain(File file);

  File write(OneOfPojaConf oneOfPojaConf);
}
