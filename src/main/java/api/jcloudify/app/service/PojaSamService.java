package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.Metadata;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.*;
import static java.net.http.HttpResponse.*;

@Service
public class PojaSamService {
    private final URI appUri;
    private final ObjectMapper objectMapper;

    public PojaSamService(@Value("${poja.sam.app.uri}") String appUri, ObjectMapper objectMapper) {
        this.appUri = URI.create(appUri);
        this.objectMapper = objectMapper;
    }

    //TODO: GET to the generated CodeUri.uri and push the code to github

    private CodeUri gen(Metadata metadata) {
        HttpClient httpClient = HttpClient.newHttpClient();
        Builder request = newBuilder();
        try {
            String serializedMetadata = serializeMetadata(metadata);
            HttpResponse<String> response = httpClient.send(
                    request.uri(appUri).POST(BodyPublishers.ofString(serializedMetadata)).build(),
                    BodyHandlers.ofString()
            );
            return deserializeCodeUri(response.body());
        } catch (InterruptedException | IOException e) {
            throw new InternalServerErrorException(e);
        }
    }

    private String serializeMetadata(Metadata metadata) throws JsonProcessingException {
        return objectMapper.writeValueAsString(metadata);
    }

    private CodeUri deserializeCodeUri(String serializedCodeUri) throws JsonProcessingException {
        return objectMapper.readValue(serializedCodeUri, CodeUri.class);
    }

    @AllArgsConstructor
    private class CodeUri {
        private final String uri;
    }
}
