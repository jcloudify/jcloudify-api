package api.jcloudify.app.service.github.model;

public record GhAppInstallation(
    long appId, String ownerGithubLogin, String type, String avatarUrl) {}
