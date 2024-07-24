package api.jcloudify.app.service.appEnvConfigurer.writer;

import static api.jcloudify.app.model.PojaVersion.POJA_V16_2_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.model.AuroraConfV1621;
import api.jcloudify.app.endpoint.rest.model.ClientConfV1621;
import api.jcloudify.app.endpoint.rest.model.ComputeConfV1621;
import api.jcloudify.app.endpoint.rest.model.ConcurrencyConfV1621;
import api.jcloudify.app.endpoint.rest.model.CustomConfV1621;
import api.jcloudify.app.endpoint.rest.model.DatabaseConfV1621;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1621;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

class AbstractAppEnvConfigMapperTest extends FacadeIT {
  public static final String POJA_V_16_2_1_YML_RESOURCE_PATH = "files/poja_v16_2_1.yml";
  @Autowired AbstractAppEnvConfigMapper subject;

  @Test
  void createNamedTempFile() {
    String randomNameWithYamlExtension = randomUUID() + ".yml";

    var actualFile = subject.createNamedTempFile(randomNameWithYamlExtension);

    assertEquals(randomNameWithYamlExtension, actualFile.getName());
  }

  @Test
  void writeToTempFile() throws IOException {
    var pojaV1331File = subject.writeToTempFile(getValidPojaConfV16_2_1());

    assertEquals(
        getResource(POJA_V_16_2_1_YML_RESOURCE_PATH).getContentAsString(UTF_8),
        readFileContent(pojaV1331File));
  }

  @Test
  void read_ok() throws IOException {
    var file = getResource(POJA_V_16_2_1_YML_RESOURCE_PATH).getFile();
    var expected = new OneOfPojaConf(getValidPojaConfV16_2_1());

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

  private static PojaConfV1621 getValidPojaConfV16_2_1() {
    return new PojaConfV1621()
        .version(POJA_V16_2_1.toHumanReadableValue())
        .aurora(new AuroraConfV1621())
        .client(new ClientConfV1621())
        .compute(new ComputeConfV1621())
        .concurrency(new ConcurrencyConfV1621())
        .database(new DatabaseConfV1621())
        .custom(new CustomConfV1621());
  }
}
