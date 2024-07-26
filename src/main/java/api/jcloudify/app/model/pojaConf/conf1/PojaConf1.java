package api.jcloudify.app.model.pojaConf.conf1;

import static api.jcloudify.app.endpoint.rest.model.PojaConf1.JSON_PROPERTY_GENERAL;

import api.jcloudify.app.endpoint.rest.model.GeneralPojaConf1;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.pojaConf.NetworkingConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import lombok.Builder;

@JsonSerialize(using = PojaConf1.PojaConf1Serializer.class)
public record PojaConf1(
    @JsonIgnoreProperties(JSON_PROPERTY_GENERAL)
        api.jcloudify.app.endpoint.rest.model.PojaConf1 rest,
    @JsonProperty(JSON_PROPERTY_GENERAL) GeneralConf1 general,
    NetworkingConfig networking)
    implements Serializable {
  @Builder
  public PojaConf1(
      api.jcloudify.app.endpoint.rest.model.PojaConf1 rest,
      NetworkingConfig networking,
      String pojaPythonRepositoryName,
      String pojaPythonRepositoryDomain,
      String pojaDomainOwner) {
    this(
        rest,
        from(
            rest.getGeneral(),
            pojaPythonRepositoryName,
            pojaPythonRepositoryDomain,
            pojaDomainOwner),
        networking);
  }

  private static GeneralConf1 from(
      GeneralPojaConf1 rest,
      String pojaPythonRepositoryName,
      String pojaPythonRepositoryDomain,
      String pojaDomainOwner) {
    return new GeneralConf1(
        rest, pojaPythonRepositoryName, pojaPythonRepositoryDomain, pojaDomainOwner);
  }

  public static class PojaConf1Serializer extends StdSerializer<PojaConf1> {
    public PojaConf1Serializer(Class<PojaConf1> t) {
      super(t);
    }

    public PojaConf1Serializer() {
      this(null);
    }

    @Override
    public void serialize(PojaConf1 value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException {
      ObjectMapper mapper = (ObjectMapper) jgen.getCodec();
      jgen.writeStartObject();

      // Flatten the fields of `rest`
      if (value.rest != null) {
        Map<String, Object> restMap = mapper.convertValue(value.rest(), new TypeReference<>() {});
        for (Map.Entry<String, Object> entry : restMap.entrySet()) {
          if (!entry.getKey().equals("general")) { // Skip rest.general for now
            jgen.writeObjectField(entry.getKey(), entry.getValue());
          }
        }
      }

      // Merge `rest.general` with `general`
      Map<String, Object> generalMap =
          value.general() != null
              ? mapper.convertValue(value.general(), new TypeReference<>() {})
              : null;
      Map<String, Object> restGeneralMap =
          value.rest() != null && value.rest().getGeneral() != null
              ? mapper.convertValue(value.rest().getGeneral(), new TypeReference<>() {})
              : null;

      if (restGeneralMap != null) {
        for (Map.Entry<String, Object> entry : restGeneralMap.entrySet()) {
          generalMap.putIfAbsent(entry.getKey(), entry.getValue());
        }
      }

      if (generalMap != null) {
        jgen.writeObjectField("general", generalMap);
      }

      // Serialize `networking` as a nested object
      if (value.networking() != null) {
        jgen.writeObjectField("networking", value.networking());
      }

      jgen.writeEndObject();
    }
  }

  public static class GeneralConf1 extends GeneralPojaConf1 {
    public static final String JSON_PROPERTY_CLI_VERSION = "cli_version";

    @JsonProperty(JSON_PROPERTY_CLI_VERSION)
    private final String cliVersion;

    @Builder
    public GeneralConf1(
        GeneralPojaConf1 rest,
        String pojaPythonRepositoryName,
        String pojaPythonRepositoryDomain,
        String pojaDomainOwner) {
      super();
      this.cliVersion = PojaVersion.POJA_1.getCliVersion();
      this.pojaPythonRepositoryName = pojaPythonRepositoryName;
      this.pojaPythonRepositoryDomain = pojaPythonRepositoryDomain;
      this.pojaDomainOwner = pojaDomainOwner;
      setAllFrom(rest);
    }

    public void setAllFrom(GeneralPojaConf1 general) {
      this.appName(general.getAppName());
      this.customJavaDeps(general.getCustomJavaDeps());
      this.customJavaEnvVars(general.getCustomJavaEnvVars());
      this.customJavaRepositories(general.getCustomJavaRepositories());
      this.packageFullName(general.getPackageFullName());
      this.withQueuesNb(general.getWithQueuesNb());
      this.withSnapstart(general.getWithSnapstart());
    }

    public static final String POJA_PYTHON_REPOSITORY_NAME = "poja_python_repository_name";
    public static final String POJA_PYTHON_REPOSITORY_DOMAIN = "poja_python_repository_domain";
    public static final String POJA_DOMAIN_OWNER = "poja_domain_owner";

    @JsonProperty(POJA_PYTHON_REPOSITORY_NAME)
    private final String pojaPythonRepositoryName;

    @JsonProperty(POJA_PYTHON_REPOSITORY_DOMAIN)
    private final String pojaPythonRepositoryDomain;

    @JsonProperty(POJA_DOMAIN_OWNER)
    private final String pojaDomainOwner;
  }
}
