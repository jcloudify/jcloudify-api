package api.jcloudify.app.endpoint.rest.mapper;

import static java.util.UUID.randomUUID;

import api.jcloudify.app.endpoint.rest.model.CreateSsmParameter;
import api.jcloudify.app.endpoint.rest.model.SsmParameter;
import api.jcloudify.app.model.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.model.Parameter;

@Component
public class SsmParameterMapper {
  public SsmParameter toRest(api.jcloudify.app.repository.model.SsmParameter domain, String value) {
    return new SsmParameter().id(domain.getId()).name(domain.getName()).value(value);
  }

  public List<SsmParameter> toRest(
      List<api.jcloudify.app.repository.model.SsmParameter> domain, List<Parameter> ssmModels) {
    return domain.stream()
        .map(parameter -> toRest(parameter, getParamValue(ssmModels, parameter.getName())))
        .toList();
  }

  private String getParamValue(List<Parameter> ssmParameters, String name) {
    Optional<Parameter> parameter =
        ssmParameters.stream().filter(p -> p.name().equals(name)).findFirst();
    return parameter
        .map(Parameter::value)
        .orElseThrow(() -> new NotFoundException("Parameter " + name + " not found"));
  }

  public api.jcloudify.app.repository.model.SsmParameter toDomain(SsmParameter rest, String envId) {
    return api.jcloudify.app.repository.model.SsmParameter.builder()
        .id(rest.getId())
        .name(rest.getName())
        .environmentId(envId)
        .build();
  }

  public api.jcloudify.app.repository.model.SsmParameter toDomain(
      CreateSsmParameter rest, String envId) {
    return api.jcloudify.app.repository.model.SsmParameter.builder()
        .id(randomUUID().toString())
        .name(rest.getName())
        .environmentId(envId)
        .build();
  }

  public List<api.jcloudify.app.repository.model.SsmParameter> toDomain(
      List<SsmParameter> rest, String envId) {
    return rest.stream().map(ssmParameter -> this.toDomain(ssmParameter, envId)).toList();
  }

  public List<api.jcloudify.app.repository.model.SsmParameter> toDomainToCreate(
      List<CreateSsmParameter> rest, String envId) {
    return rest.stream().map(ssmParameter -> this.toDomain(ssmParameter, envId)).toList();
  }
}
