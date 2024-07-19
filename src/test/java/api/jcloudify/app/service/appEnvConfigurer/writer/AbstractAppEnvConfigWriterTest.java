package api.jcloudify.app.service.appEnvConfigurer.writer;

import static api.jcloudify.app.model.PojaVersion.POJA_V13_3_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.model.AuroraConfV1331;
import api.jcloudify.app.endpoint.rest.model.ClientConfV1331;
import api.jcloudify.app.endpoint.rest.model.ComputeConfV1331;
import api.jcloudify.app.endpoint.rest.model.ConcurrencyConfV1331;
import api.jcloudify.app.endpoint.rest.model.CustomConfV1331;
import api.jcloudify.app.endpoint.rest.model.DatabaseConfV1331;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1331;
import java.io.File;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

class AbstractAppEnvConfigWriterTest extends FacadeIT {
  @Autowired AbstractAppEnvConfigWriter subject;

  @Test
  void createNamedTempFile() {
    String randomNameWithYamlExtension = randomUUID() + ".yml";

    var actualFile = subject.createNamedTempFile(randomNameWithYamlExtension);

    assertEquals(randomNameWithYamlExtension, actualFile.getName());
  }

  @Test
  void writeToTempFile() {
    var pojaV1331File = subject.writeToTempFile(getValidPojaConfV13_3_1());

    assertEquals(getResourceFileAsString("files/poja_v13_3_1.yml"), readFileContent(pojaV1331File));
  }

  @SneakyThrows
  private static String readFileContent(File file) {
    return readBytesToString(readAllBytes(Path.of(file.getAbsolutePath())));
  }

  private static String readBytesToString(byte[] bytes) {
    return new String(bytes);
  }

  @SneakyThrows
  private String getResourceFileAsString(String resourceFilePath) {
    var classPathResource = new ClassPathResource(resourceFilePath);
    return classPathResource.getContentAsString(UTF_8);
  }

  private static PojaConfV1331 getValidPojaConfV13_3_1() {
    return new PojaConfV1331()
        .version(POJA_V13_3_1.toHumanReadableValue())
        .aurora(new AuroraConfV1331())
        .client(new ClientConfV1331())
        .compute(new ComputeConfV1331())
        .concurrency(new ConcurrencyConfV1331())
        .database(new DatabaseConfV1331())
        .custom(new CustomConfV1331());
  }
}
