package api.jcloudify.app.service.pojaConfHandler;

import static api.jcloudify.app.model.PojaVersion.POJA_1;

import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.exception.NotImplementedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Slf4j
public class PojaConfUploadedHandlerFacade implements PojaConfUploadedHandler {
  private final PojaConfUploadedHandler poja1UploadedHandler;

  public PojaConfUploadedHandlerFacade(
      @Qualifier("poja1UploadedHandler") PojaConfUploadedHandler poja1UploadedHandler) {
    this.poja1UploadedHandler = poja1UploadedHandler;
  }

  @Override
  public void handlePojaConfUploaded(PojaConfUploaded pojaConfUploaded) {
    getPojaConfUploadedHandler(pojaConfUploaded.getPojaVersion())
        .handlePojaConfUploaded(pojaConfUploaded);
  }

  private PojaConfUploadedHandler getPojaConfUploadedHandler(PojaVersion pojaVersion) {
    if (POJA_1.equals(pojaVersion)) {
      return poja1UploadedHandler;
    }
    throw new NotImplementedException("not implemented");
  }
}
