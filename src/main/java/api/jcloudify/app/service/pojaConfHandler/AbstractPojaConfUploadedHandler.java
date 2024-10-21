package api.jcloudify.app.service.pojaConfHandler;

import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.pojaConf.conf1.PojaConf;
import api.jcloudify.app.service.appEnvConfigurer.AppEnvConfigurerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractPojaConfUploadedHandler implements PojaConfUploadedHandler {
  private final PojaVersion pojaVersion;
  private final AppEnvConfigurerService appEnvConfigurerService;

  protected AbstractPojaConfUploadedHandler(
      PojaVersion pojaVersion, AppEnvConfigurerService appEnvConfigurerService) {
    this.pojaVersion = pojaVersion;
    this.appEnvConfigurerService = appEnvConfigurerService;
  }

  protected abstract void handlePojaConf(PojaConfUploaded pojaConfUploaded, PojaConf pojaConf);

  @Override
  public final void handlePojaConfUploaded(PojaConfUploaded pojaConfUploaded) {
    if (!this.pojaVersion.equals(pojaConfUploaded.getPojaVersion())) {
      log.error(
          "expected Poja version {} does not match poja version {}", this.pojaVersion, pojaVersion);
      return;
    }
    var domain =
        appEnvConfigurerService.readConfigAsDomain(
            pojaConfUploaded.getUserId(),
            pojaConfUploaded.getAppId(),
            pojaConfUploaded.getEnvironmentId(),
            pojaConfUploaded.getFilename());
    handlePojaConf(pojaConfUploaded, domain);
  }
}
