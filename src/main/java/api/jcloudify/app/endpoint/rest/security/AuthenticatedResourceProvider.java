package api.jcloudify.app.endpoint.rest.security;

import api.jcloudify.app.repository.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthenticatedResourceProvider {
    public User getUser() {
        return AuthProvider.getPrincipal().getUser();
    }
}
