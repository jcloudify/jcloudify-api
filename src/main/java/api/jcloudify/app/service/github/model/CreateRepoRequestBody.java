package api.jcloudify.app.service.github.model;

public record CreateRepoRequestBody(String name, String description, boolean isPrivate) {}
