package api.jcloudify.app.service.appEnvConfigurer.writer;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import api.jcloudify.app.endpoint.rest.model.PojaConfV1331;
import api.jcloudify.app.service.appEnvConfigurer.writer.mixins.PojaConfV13_3_1Mixin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FileWriterConf {
  @Bean("yamlObjectMapper")
  public ObjectMapper yamlObjectMapper() {
    ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    yamlObjectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    yamlObjectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
    yamlObjectMapper.findAndRegisterModules();
    yamlObjectMapper.addMixIn(PojaConfV1331.class, PojaConfV13_3_1Mixin.class);
    return yamlObjectMapper;
  }
}
