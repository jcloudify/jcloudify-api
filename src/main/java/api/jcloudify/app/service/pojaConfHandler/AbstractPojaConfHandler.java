package api.jcloudify.app.service.pojaConfHandler;

import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.model.PojaVersion;

public abstract class AbstractPojaConfHandler implements PojaConfHandler {
  private final PojaVersion pojaVersion;

  protected AbstractPojaConfHandler(PojaVersion pojaVersion) {
    this.pojaVersion = pojaVersion;
  }

  @Override
  public final void handlePojaConf(PojaVersion pojaVersion, PojaConf pojaConf) {
    handlePojaConf(pojaConf);
  }

  public abstract void handlePojaConf(PojaConf pojaConf);
}
