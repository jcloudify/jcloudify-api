package api.jcloudify.app.model;

import static java.net.URI.create;

import java.net.URI;
import lombok.Getter;

@Getter
public enum PojaVersion implements Comparable<PojaVersion> {
  POJA_V16_2_1(
      16, 2, 1, create("https://u2csjwdclz55oe4tivon6xtzli0ctjap.lambda-url.eu-west-3.on.aws"));
  private final int major;
  private final int minor;
  private final int patch;
  private final URI samUri;

  PojaVersion(int major, int minor, int patch, URI samUri) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.samUri = samUri;
  }

  public final String toHumanReadableValue() {
    return String.format("%d.%d.%d", major, minor, patch);
  }
}
