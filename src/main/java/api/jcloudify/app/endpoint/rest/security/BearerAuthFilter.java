package api.jcloudify.app.endpoint.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

public class BearerAuthFilter extends AbstractAuthenticationProcessingFilter {

    private final String authorizationHeader;

    protected BearerAuthFilter(RequestMatcher defaultFilterProcessesUrl, String authorizationHeader) {
        super(defaultFilterProcessesUrl);
        this.authorizationHeader = authorizationHeader;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String bearer = request.getHeader(authorizationHeader);
        return getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(bearer, bearer));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
