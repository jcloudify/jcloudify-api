package api.jcloudify.app.service.pojaConfHandler;

import static api.jcloudify.app.model.PojaVersion.POJA_1;

import api.jcloudify.app.endpoint.rest.model.PojaConf;
import org.springframework.stereotype.Component;

@Component
public class Poja1Handler extends AbstractPojaConfHandler {
  protected Poja1Handler() {
    super(POJA_1);
  }

  @Override
  public void handlePojaConf(PojaConf pojaConf) {

  }
}
