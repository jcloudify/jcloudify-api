package api.jcloudify.app.service.pojaConfHandler;

import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;

public interface PojaConfUploadedHandler {
  void handlePojaConfUploaded(PojaConfUploaded pojaConfUploaded);
}
