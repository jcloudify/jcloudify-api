package api.jcloudify.app.repository.impl;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.repository.PojaVersionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PojaVersionRepositoryImpl implements PojaVersionRepository {

  private static final String POJA_VERSIONS_RESOURCE_FILE_PATH = "files/poja_versions.json";
  private static final TypeReference<List<PojaVersion>> POJA_VERSIONS_TYPE_REF =
      new TypeReference<>() {};
  private final ObjectMapper om;

  @Override
  public List<PojaVersion> findAll() {
    Resource classPathResource = new ClassPathResource(POJA_VERSIONS_RESOURCE_FILE_PATH);
    try (var resourceStream = classPathResource.getInputStream()) {
      return om.readValue(resourceStream, POJA_VERSIONS_TYPE_REF);
    } catch (IOException e) {
      throw new ApiException(
          SERVER_EXCEPTION, "unable to open " + POJA_VERSIONS_RESOURCE_FILE_PATH);
    }
  }
}
