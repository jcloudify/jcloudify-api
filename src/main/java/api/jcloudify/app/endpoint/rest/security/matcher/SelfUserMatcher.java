package api.jcloudify.app.endpoint.rest.security.matcher;

import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class SelfUserMatcher extends SelfMatcher {
  public SelfUserMatcher(
      HttpMethod method, String antPattern, AuthenticatedResourceProvider authResourceProvider) {
    super(method, antPattern, authResourceProvider);
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    AntPathRequestMatcher antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    return authResourceProvider.getUser().getId().equals(getId(request));
  }
}
