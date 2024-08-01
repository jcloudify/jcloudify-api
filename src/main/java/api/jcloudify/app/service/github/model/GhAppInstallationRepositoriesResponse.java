package api.jcloudify.app.service.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhAppInstallationRepositoriesResponse(
        @JsonProperty("total_count") long totalCount, @JsonProperty("repositories") List<GHAppInstallationRepository> repositories) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GHAppInstallationRepository(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("html_url") String htmlUrl) {}
}
