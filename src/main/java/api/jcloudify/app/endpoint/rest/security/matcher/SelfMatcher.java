package api.jcloudify.app.endpoint.rest.security.matcher;

import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;

@AllArgsConstructor
public abstract class SelfMatcher implements RequestMatcher {
  private static final Pattern SELFABLE_URI_PATTERN =
      // /resourceType/id/...
      Pattern.compile("/[^/]+/(?<id>[^/]+)(/.*)?");
  private static final String GROUP_NAME = "id";
  protected final HttpMethod method;
  protected final String antPattern;
  protected final AuthenticatedResourceProvider authResourceProvider;

  protected String getId(HttpServletRequest request) {
    Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group(GROUP_NAME) : null;
  }
}
