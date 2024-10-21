package api.jcloudify.app.service.event;

import static api.jcloudify.app.endpoint.event.model.NotifyPojaConfUploaded.Status.FAILURE;
import static api.jcloudify.app.endpoint.event.model.NotifyPojaConfUploaded.Status.SUCCESS;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.NotifyPojaConfUploaded;
import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.service.pojaConfHandler.PojaConfUploadedHandler;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PojaConfUploadedService implements Consumer<PojaConfUploaded> {
  public static final String JCLOUDIFY_BOT_USERNAME = "jcloudify[bot]";
  private final EventProducer<NotifyPojaConfUploaded> eventProducer;
  private final PojaConfUploadedHandler pojaConfUploadedHandler;

  @Override
  public void accept(PojaConfUploaded pojaConfUploaded) {
    try {
      Objects.requireNonNull(handlePojaConfUploaded(pojaConfUploaded)).call();
      handleEventSuccess(pojaConfUploaded);
      log.info("Success at PojaConfUploaded");
    } catch (Exception e) {
      log.info("Failure at PojaConfUploaded");
      log.error("Error Message: {}", e.getMessage());
      log.error("Stacktrace: {}", (Object) e.getStackTrace());
      log.error("Error Cause: {}", e.getCause().toString());
      handleEventFailure(pojaConfUploaded);
      throw new RuntimeException(e);
    }
  }

  private void handleEventFailure(PojaConfUploaded pojaConfUploaded) {
    eventProducer.accept(
        List.of(
            NotifyPojaConfUploaded.builder()
                .status(FAILURE)
                .appId(pojaConfUploaded.getAppId())
                .envId(pojaConfUploaded.getEnvironmentId())
                .build()));
  }

  private void handleEventSuccess(PojaConfUploaded pojaConfUploaded) {
    eventProducer.accept(
        List.of(
            NotifyPojaConfUploaded.builder()
                .status(SUCCESS)
                .appId(pojaConfUploaded.getAppId())
                .envId(pojaConfUploaded.getEnvironmentId())
                .build()));
  }

  private Callable<Void> handlePojaConfUploaded(PojaConfUploaded pojaConfUploaded) {
    return () -> {
      pojaConfUploadedHandler.handlePojaConfUploaded(pojaConfUploaded);
      return null;
    };
  }
}
