package api.jcloudify.app.service.appEnvConfigurer.writer.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
public abstract class PojaConfV13_3_1Mixin {
  @JsonProperty("cli_version")
  private String version = "13.3.1";
}
