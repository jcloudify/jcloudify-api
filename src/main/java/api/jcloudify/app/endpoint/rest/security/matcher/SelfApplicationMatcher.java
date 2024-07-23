package api.jcloudify.app.endpoint.rest.security.matcher;

import api.jcloudify.app.endpoint.rest.security.AuthProvider;
import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class SelfApplicationMatcher implements RequestMatcher {
    private final ApplicationService applicationService;
    private static final Pattern SELFABLE_URI_PATTERN =
            // /users/id/applications/id/...
            Pattern.compile("/[^/]+/(?<userId>[^/]+)/[^/]+/(?<applicationId>[^/]+)(/.*)?");
    protected final HttpMethod method;
    protected final String antPattern;
    protected final AuthenticatedResourceProvider authResourceProvider;

    @Override
    public boolean matches(HttpServletRequest request) {
        AntPathRequestMatcher antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
        if (!antMatcher.matches(request)) {
            return false;
        }
        var selfUserId = getSelfUserId(request);
        var selfApplicationId = getSelfApplicationId(request);
        var authenticatedUserId = AuthProvider.getPrincipal().getUser().getId();
        assert selfUserId != null;
        if (selfUserId.equals(authenticatedUserId)) {
            Optional<Application> application = applicationService.findById(selfApplicationId);
            return application.isPresent() && application.get().getUserId().equals(selfUserId);
        }
        return false;
    }

    private String getSelfUserId(HttpServletRequest request) {
        Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
        return uriMatcher.find() ? uriMatcher.group("userId") : null;
    }

    private String getSelfApplicationId(HttpServletRequest request) {
        Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
        return uriMatcher.find() ? uriMatcher.group("applicationId") : null;
    }
}
