package api.jcloudify.app.service;

import api.jcloudify.app.repository.jpa.AppInstallationRepository;
import api.jcloudify.app.repository.model.AppInstallation;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppInstallationService {
  private final AppInstallationRepository repository;

  public List<AppInstallation> saveAll(List<AppInstallation> toSave) {
    return repository.saveAll(toSave);
  }

  public List<AppInstallation> findAllByUserId(String userId) {
    return repository.findAllByUserId(userId);
  }
}
