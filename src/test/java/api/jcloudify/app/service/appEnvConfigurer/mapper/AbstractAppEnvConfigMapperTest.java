package api.jcloudify.app.service.appEnvConfigurer.mapper;

import static api.jcloudify.app.model.PojaVersion.POJA_V17_0_0;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.model.ComputeConfV1700;
import api.jcloudify.app.endpoint.rest.model.ConcurrencyConfV1700;
import api.jcloudify.app.endpoint.rest.model.DatabaseConfV1700;
import api.jcloudify.app.endpoint.rest.model.GenApiClientV1700;
import api.jcloudify.app.endpoint.rest.model.GeneralPojaConfV1700;
import api.jcloudify.app.endpoint.rest.model.IntegrationV1700;
import api.jcloudify.app.endpoint.rest.model.MailingConfV1700;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1700;
import api.jcloudify.app.endpoint.rest.model.TestingConfV1700;
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

  private static PojaConfV1700 getValidPojaConfV17_0_0() {
    String humanReadableValuePojaVersion = POJA_V17_0_0.toHumanReadableValue();
    return new PojaConfV1700()
        .version(humanReadableValuePojaVersion)
        .general(new GeneralPojaConfV1700().cliVersion(humanReadableValuePojaVersion))
        .database(new DatabaseConfV1700())
        .emailing(new MailingConfV1700())
        .genApiClient(new GenApiClientV1700())
        .integration(new IntegrationV1700())
        .compute(new ComputeConfV1700())
        .concurrency(new ConcurrencyConfV1700())
        .testing(new TestingConfV1700());
  }
}
