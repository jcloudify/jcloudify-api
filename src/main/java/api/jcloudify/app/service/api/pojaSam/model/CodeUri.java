package api.jcloudify.app.service.api.pojaSam.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public record CodeUri(@JsonProperty("uri") URI uri) {}
