package api.jcloudify.app.service.appEnvConfigurer.writer;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

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
    return yamlObjectMapper;
  }
}
