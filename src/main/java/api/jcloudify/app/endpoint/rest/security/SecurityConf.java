package api.jcloudify.app.endpoint.rest.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import api.jcloudify.app.model.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConf {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private final AuthProvider authProvider;
  private final HandlerExceptionResolver exceptionResolver;

  public SecurityConf(
      AuthProvider authProvider,
      // InternalToExternalErrorHandler behind
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.exceptionResolver = exceptionResolver;
    this.authProvider = authProvider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.exceptionHandling(
            (exceptionHandler) ->
                exceptionHandler
                    .authenticationEntryPoint(
                        // note(spring-exception)
                        // https://stackoverflow.com/questions/59417122/how-to-handle-usernamenotfoundexception-spring-security
                        // issues like when a user tries to access a resource
                        // without appropriate authentication elements
                        (req, res, e) ->
                            exceptionResolver.resolveException(
                                req, res, null, forbiddenWithRemoteInfo(e, req)))
                    .accessDeniedHandler(
                        // note(spring-exception): issues like when a user not having required roles
                        (req, res, e) ->
                            exceptionResolver.resolveException(
                                req, res, null, forbiddenWithRemoteInfo(e, req))))
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .authenticationProvider(authProvider)
        .addFilterBefore(
            bearerFilter(
                new OrRequestMatcher(
                    antMatcher(GET, "/whoami"),
                    antMatcher(POST, "/applications/*/environments/*/deploymentInitiation"),
                    antMatcher(PUT, "/applications"),
                    antMatcher(GET, "/applications"),
                    antMatcher(GET, "/poja-versions"),
                    antMatcher(GET, "/applications/*/environments"),
                    antMatcher(PUT, "/applications/*/environments"))),
            AnonymousAuthenticationFilter.class)
        .authorizeHttpRequests(
            (authorize) ->
                authorize
                    .requestMatchers(OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(GET, "/ping")
                    .permitAll()
                    .requestMatchers(GET, "/token")
                    .permitAll()
                    .requestMatchers(GET, "/health/db")
                    .permitAll()
                    .requestMatchers(GET, "/health/bucket")
                    .permitAll()
                    .requestMatchers(POST, "/health/event/uuids")
                    .permitAll()
                    .requestMatchers(GET, "/health/event1")
                    .permitAll()
                    .requestMatchers(GET, "/health/event2")
                    .permitAll()
                    .requestMatchers(GET, "/health/email")
                    .permitAll()
                    .requestMatchers(POST, "/users")
                    .permitAll()
                    .requestMatchers(GET, "/whoami")
                    .authenticated()
                    .requestMatchers(GET, "/poja-versions")
                    .authenticated()
                    .requestMatchers(PUT, "/applications")
                    .authenticated()
                    .requestMatchers(GET, "/applications")
                    .authenticated()
                    .requestMatchers(GET, "/applications/*/environments")
                    .authenticated()
                    .requestMatchers(PUT, "/applications/*/environments")
                    .authenticated()
                    .requestMatchers(POST, "/applications/*/environments/*/deploymentInitiation")
                    .authenticated()
                    .requestMatchers(GET, "/users/*/payment-methods")
                    .authenticated()
                    .requestMatchers(POST, "/users/*/payment-methods/customers")
                    .authenticated()
                    .requestMatchers(PUT, "/users/*/payment-methods/**")
                    .authenticated()
                    .requestMatchers("/**")
                    .denyAll())
        // disable superfluous protections
        // Eg if all clients are non-browser then no csrf
        // https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/csrf.html,
        // Sec 13.3
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable);
    // formatter:on
    return http.build();
  }

  private Exception forbiddenWithRemoteInfo(Exception e, HttpServletRequest req) {
    log.info(
        String.format(
            "Access is denied for remote caller: address=%s, host=%s, port=%s",
            req.getRemoteAddr(), req.getRemoteHost(), req.getRemotePort()));
    return new ForbiddenException(e.getMessage());
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(authProvider);
  }

  private BearerAuthFilter bearerFilter(RequestMatcher requestMatcher) throws Exception {
    BearerAuthFilter bearerFilter = new BearerAuthFilter(requestMatcher, AUTHORIZATION_HEADER);
    bearerFilter.setAuthenticationManager(authenticationManager());
    bearerFilter.setAuthenticationSuccessHandler(
        (httpServletRequest, httpServletResponse, authentication) -> {});
    bearerFilter.setAuthenticationFailureHandler(
        (req, res, e) ->
            // note(spring-exception)
            // issues like when a user is not found(i.e. UsernameNotFoundException)
            // or other exceptions thrown inside authentication provider.
            // In fact, this handles other authentication exceptions that are
            // not handled by AccessDeniedException and AuthenticationEntryPoint
            exceptionResolver.resolveException(req, res, null, forbiddenWithRemoteInfo(e, req)));
    return bearerFilter;
  }
}
