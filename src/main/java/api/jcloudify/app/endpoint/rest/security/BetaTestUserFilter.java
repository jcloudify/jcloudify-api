package api.jcloudify.app.endpoint.rest.security;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import api.jcloudify.app.endpoint.rest.security.model.Principal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class BetaTestUserFilter extends OncePerRequestFilter {
  private final RequestMatcher requiresIsBetaTestUserRequestMatchers;

  public BetaTestUserFilter(RequestMatcher requiresIsBetaTestUserRequestMatchers) {
    this.requiresIsBetaTestUserRequestMatchers = requiresIsBetaTestUserRequestMatchers;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (requiresIsBetaTestUserRequestMatchers.matches(request)) {
      var authentication = AuthProvider.getAuthentication().getPrincipal();
      if (authentication instanceof Principal principal) {
        if (!principal.isBetaTester()) {
          response.sendError(SC_FORBIDDEN);
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
