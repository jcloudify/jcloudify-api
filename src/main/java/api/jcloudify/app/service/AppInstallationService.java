package api.jcloudify.app.service;

import api.jcloudify.app.repository.jpa.AppInstallationRepository;
import api.jcloudify.app.repository.model.AppInstallation;
import api.jcloudify.app.service.github.GithubService;
import api.jcloudify.app.service.github.model.GhAppInstallation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppInstallationService {
  private final AppInstallationRepository repository;
  private final GithubService githubService;

  public List<AppInstallation> saveAll(List<AppInstallation> toSave) {
    return repository.saveAll(toSave);
  }

  public List<AppInstallation> findAllByUserId(String userId) {
    var githubAppInstallations = githubService.listApplications();
    var persisted = repository.findAllByUserId(userId);
    Set<Long> mappedInstallations =
        githubAppInstallations.stream()
            .map(GhAppInstallation::appId)
            .collect(Collectors.toUnmodifiableSet());
    return persisted.stream().filter((a) -> mappedInstallations.contains(a.getGhId())).toList();
  }
}
