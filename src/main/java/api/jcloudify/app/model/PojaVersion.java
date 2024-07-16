package api.jcloudify.app.model;

import java.net.URI;
import lombok.Builder;

@Builder
public record PojaVersion(int major, int minor, int patch, URI samUri /*(ignored in compareTo)*/)
    implements Comparable<PojaVersion> {
  @Override
  public int compareTo(PojaVersion o) {
    var majorComparison = Integer.compare(major, o.major);
    if (majorComparison != 0) {
      return majorComparison;
    }
    var minorComparison = Integer.compare(minor, o.minor);
    if (minorComparison != 0) {
      return minorComparison;
    }
    return Integer.compare(patch, o.patch);
  }
}
