package api.jcloudify.app.service.appEnvConfigurer.writer.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
public abstract class PojaConfV16_2_1Mixin {
  public static final String CLI_VERSION_ATTRIBUTE = "cli_version";

  @JsonProperty(CLI_VERSION_ATTRIBUTE)
  private String version = "16.2.1";
}
