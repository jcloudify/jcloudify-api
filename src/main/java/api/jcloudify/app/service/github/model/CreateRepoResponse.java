package api.jcloudify.app.service.github.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public record CreateRepoResponse(
    String id,
    String name,
    @JsonProperty("full_name") String fullName,
    String description,
    @JsonProperty("html_url") URI htmlUrl,
    boolean isPrivate) {}
