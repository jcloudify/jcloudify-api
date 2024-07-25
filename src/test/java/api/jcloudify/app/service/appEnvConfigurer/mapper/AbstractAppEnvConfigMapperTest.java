package api.jcloudify.app.service.appEnvConfigurer.mapper;

import static api.jcloudify.app.integration.conf.utils.TestMocks.getValidPojaConfV17_0_0;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

class AbstractAppEnvConfigMapperTest extends FacadeIT {
  public static final String POJA_V_17_0_0_YML_RESOURCE_PATH = "files/poja_v17_0_0.yml";
  @Autowired AbstractAppEnvConfigMapper subject;

  @Test
  void createNamedTempFile() {
    String randomNameWithYamlExtension = randomUUID() + ".yml";

    var actualFile = subject.createNamedTempFile(randomNameWithYamlExtension);

    assertEquals(randomNameWithYamlExtension, actualFile.getName());
  }

  @Test
  void writeToTempFile() throws IOException {
    var pojaV1331File = subject.writeToTempFile(getValidPojaConfV17_0_0());

    assertEquals(
        getResource(POJA_V_17_0_0_YML_RESOURCE_PATH).getContentAsString(UTF_8),
        readFileContent(pojaV1331File));
  }

  @Test
  void read_ok() throws IOException {
    var file = getResource(POJA_V_17_0_0_YML_RESOURCE_PATH).getFile();
    var expected = new OneOfPojaConf(getValidPojaConfV17_0_0());

    var actual = subject.read(file);

    assertEquals(expected, actual);
  }

  @SneakyThrows
  private static String readFileContent(File file) {
    return readBytesToString(readAllBytes(Path.of(file.getAbsolutePath())));
  }

  private static String readBytesToString(byte[] bytes) {
    return new String(bytes);
  }

  @SneakyThrows
  private Resource getResource(String resourceFilePath) {
    return new ClassPathResource(resourceFilePath);
  }
}
