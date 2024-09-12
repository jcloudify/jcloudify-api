package api.jcloudify.app.service.pojaConfHandler;

import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.model.PojaVersion;

public interface PojaConfHandler {
  void handlePojaConf(PojaVersion pojaVersion, PojaConf pojaConf);
}
