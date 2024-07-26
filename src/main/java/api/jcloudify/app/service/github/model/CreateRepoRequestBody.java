package api.jcloudify.app.service.github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateRepoRequestBody(
    String owner, String name, String description, @JsonProperty("private") boolean isPrivate) {}
