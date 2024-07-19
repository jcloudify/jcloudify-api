package api.jcloudify.app.model;

import static java.net.URI.create;

import java.net.URI;
import lombok.Getter;

@Getter
public enum PojaVersion implements Comparable<PojaVersion> {
  POJA_V13_3_1(
      13, 3, 1, create("https://bdzf2zjbk6vdvjtd2plid4emq40rivto.lambda-url.eu-west-3.on.aws"));
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
