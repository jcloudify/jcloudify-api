package api.jcloudify.app.model;

import static java.net.URI.create;
import static java.util.Optional.empty;

import java.net.URI;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum PojaVersion implements Comparable<PojaVersion> {
  POJA_1(3, 6, 2, create("https://g7ztepcqfgwmiumrhbhzl674o40iqdip.lambda-url.eu-west-3.on.aws")) {
    @Override
    public String getCliVersion() {
      return "17.1.2";
    }
  };
  private final int major;
  private final int minor;
  private final int patch;
  private final URI samUri;

  public abstract String getCliVersion();

  PojaVersion(int major, int minor, int patch, URI samUri) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.samUri = samUri;
  }

  public final String toHumanReadableValue() {
    return String.format("%d.%d.%d", major, minor, patch);
  }

  public static Optional<PojaVersion> fromHumanReadableValue(String humanReadableValue) {
    for (PojaVersion value : values()) {
      if (value.toHumanReadableValue().equals(humanReadableValue)) {
        return Optional.of(value);
      }
    }
    return empty();
  }

  public static Optional<PojaVersion> fromCliVersion(String cliVersion) {
    for (PojaVersion value : values()) {
      if (value.getCliVersion().equals(cliVersion)) {
        return Optional.of(value);
      }
    }
    return empty();
  }
}
