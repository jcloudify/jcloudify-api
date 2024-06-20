package api.jcloudify.app.aws.cloudformation;

import api.jcloudify.app.file.BucketComponent;
import java.net.URL;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CloudformationTemplateConf {
  private final BucketComponent bucketComponent;
  private static final String CF_STACK_TEMPLATE_FOLDER = "cf-templates/";

  public URL getEventStackTemplateUrl() {
    String eventStackTemplatePath = CF_STACK_TEMPLATE_FOLDER + "event-stack.yml";
    return bucketComponent.presign(eventStackTemplatePath, Duration.ofMinutes(5));
  }

  public URL getComputePermissionStackTemplateUrl() {
    String eventStackTemplatePath = CF_STACK_TEMPLATE_FOLDER + "compute-permission-stack.yml";
    return bucketComponent.presign(eventStackTemplatePath, Duration.ofMinutes(1));
  }

  public URL getStorageBucketStackTemplateUrl() {
    String eventStackTemplatePath = CF_STACK_TEMPLATE_FOLDER + "storage-bucket-stack.yml";
    return bucketComponent.presign(eventStackTemplatePath, Duration.ofMinutes(3));
  }

  public URL getStorageDatabaseStackTemplateUrl() {
    String eventStackTemplatePath = CF_STACK_TEMPLATE_FOLDER + "storage-database-stack.yml";
    return bucketComponent.presign(eventStackTemplatePath, Duration.ofMinutes(5));
  }
}
