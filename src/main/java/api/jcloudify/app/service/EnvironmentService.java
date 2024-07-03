package api.jcloudify.app.service;

import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironment;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.repository.jpa.EnvironmentRepository;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.mapper.EnvironmentMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EnvironmentService {
    private final EnvironmentRepository repository;
    @Qualifier("DomainEnvironmentMapper")
    private final EnvironmentMapper environmentMapper;
    private final ApplicationRepository applicationRepository;

    public List<Environment> saveEnvironments(String applicationId, List<CrupdateEnvironment> toSave) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new NotFoundException("Application with id " + applicationId + " does not exist");
        }
        return repository.saveAll(toSave.stream().map(env -> environmentMapper.toDomain(applicationId, env)).toList());
    }

    public Environment getById(String id) {
        return repository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Environment identified by id " + id + " not found"));
    }
}
