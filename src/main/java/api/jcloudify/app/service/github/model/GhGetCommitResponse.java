package api.jcloudify.app.service.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhGetCommitResponse(String sha, GhCommit commit, GhUser committer) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record GhCommit(String message, GhCommitter committer, URI url) {
    public record GhCommitter(String name, String email) {}
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record GhUser(
      String login, String id, @JsonProperty("avatar_url") URI avatarUrl, String type) {}
}
