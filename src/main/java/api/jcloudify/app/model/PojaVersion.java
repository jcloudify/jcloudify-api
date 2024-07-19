package api.jcloudify.app.model;

import static java.lang.Integer.compare;

import java.net.URI;
import lombok.Builder;

@Builder
public record PojaVersion(int major, int minor, int patch, URI samUri /*(ignored in compareTo)*/)
    implements Comparable<PojaVersion> {
  @Override
  public int compareTo(PojaVersion o) {
    var majorComparison = compare(major, o.major);
    if (majorComparison != 0) {
      return majorComparison;
    }
    var minorComparison = compare(minor, o.minor);
    if (minorComparison != 0) {
      return minorComparison;
    }
    return compare(patch, o.patch);
  }

  public String toHumanReadableString() {
    return String.format("%d.%d.%d", major, minor, patch);
  }
}
