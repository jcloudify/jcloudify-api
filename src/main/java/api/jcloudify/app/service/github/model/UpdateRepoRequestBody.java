package api.jcloudify.app.service.github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateRepoRequestBody(
    String name,
    String description,
    boolean isPrivate,
    @JsonProperty("archived") boolean isArchived) {}
