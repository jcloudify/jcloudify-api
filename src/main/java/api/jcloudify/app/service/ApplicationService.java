package api.jcloudify.app.service;

import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.ApplicationRepository;
import api.jcloudify.app.repository.model.Application;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ApplicationService {
    private final ApplicationRepository repository;

    public Application getById(String id) {
        return repository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Application identified by id=" + id + " not found"));
    }
}
