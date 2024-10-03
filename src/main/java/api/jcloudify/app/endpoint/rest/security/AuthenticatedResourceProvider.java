package api.jcloudify.app.endpoint.rest.security;

import api.jcloudify.app.model.User;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.service.ApplicationService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthenticatedResourceProvider {
  private final ApplicationService applicationService;

  public User getUser() {
    return AuthProvider.getPrincipal().getUser();
  }

  public Application getAuthenticatedApplication() {
    return AuthProvider.getApplicationPrincipal().getApplication();
  }

  public boolean isApplicationOwner(String userId, String applicationId) {
    Optional<Application> application = applicationService.findById(applicationId);
    return application.isPresent() && application.get().getUserId().equals(userId);
  }
}
