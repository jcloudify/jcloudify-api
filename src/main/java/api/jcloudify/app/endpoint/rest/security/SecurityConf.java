package api.jcloudify.app.endpoint.rest.security;

import static api.jcloudify.app.endpoint.rest.security.model.ApplicationRole.GITHUB_APPLICATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import api.jcloudify.app.endpoint.rest.security.matcher.SelfApplicationMatcher;
import api.jcloudify.app.endpoint.rest.security.matcher.SelfUserMatcher;
import api.jcloudify.app.model.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
  private final AuthenticatedResourceProvider authenticatedResourceProvider;

  public SecurityConf(
      AuthProvider authProvider,
      // InternalToExternalErrorHandler behind
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
      AuthenticatedResourceProvider authenticatedResourceProvider) {
    this.exceptionResolver = exceptionResolver;
    this.authProvider = authProvider;
    this.authenticatedResourceProvider = authenticatedResourceProvider;
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
                    antMatcher(GET, "/users/*/installations"),
                    antMatcher(PUT, "/users/*/installations"),
                    antMatcher(PUT, "/users/*/applications/*/environments/*/deploymentInitiation"),
                    antMatcher(POST, "/users/*/applications/*/environments/*/deletionInitiation"),
                    antMatcher(PUT, "/users/*/applications"),
                    antMatcher(GET, "/users/*/applications"),
                    antMatcher(GET, "/users/*/applications/*"),
                    antMatcher(GET, "/poja-versions"),
                    antMatcher(GET, "/users/*/applications/*/environments"),
                    antMatcher(PUT, "/users/*/applications/*/environments"),
                    antMatcher(GET, "/users/*/applications/*/environments/*"),
                    antMatcher(PUT, "/users/*/applications/*/environments/*/ssmparameters"),
                    antMatcher(POST, "/users/*/applications/*/environments/*/ssmparameters"),
                    antMatcher(GET, "/users/*/applications/*/environments/*/ssmparameters"),
                    antMatcher(GET, "/users/*/applications/*/environments/*/stacks"),
                    antMatcher(GET, "/users/*/applications/*/environments/*/stacks/*"),
                    antMatcher(GET, "/users/*/applications/*/environments/*/stacks/*/events"),
                    antMatcher(GET, "/users/*/applications/*/environments/*/stacks/*/outputs"),
                    antMatcher(PUT, "/users/*/applications/*/environments/*/config"),
                    antMatcher(GET, "/users/*/applications/*/environments/*/config"),
                    antMatcher(GET, "/users/*/payment-methods"),
                    antMatcher(GET, "/gh-repos/*/*/upload-build-uri"),
                    antMatcher(GET, "/users/*/payment-details"),
                    antMatcher(PUT, "/users/*/payment-details"),
                    antMatcher(GET, "/users/*/payment-details/payment-methods"),
                    antMatcher(PUT, "/users/*/payment-details/payment-methods"),
                    antMatcher(PUT, "/gh-repos/*/*/env-deploys"))),
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
                    .requestMatchers(POST, "/token")
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
                    .requestMatchers(GET, "/users/*/payment-details")
                    .authenticated()
                    .requestMatchers(PUT, "/users/*/payment-details")
                    .authenticated()
                    .requestMatchers(GET, "/users/*/payment-details/payment-methods")
                    .authenticated()
                    .requestMatchers(PUT, "/users/*/payment-details/payment-methods")
                    .authenticated()
                    .requestMatchers(selfUserMatcher(PUT, "/users/*/installations"))
                    .authenticated()
                    .requestMatchers(selfUserMatcher(GET, "/users/*/installations"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            PUT, "/users/*/applications/*/environments/*/deploymentInitiation"))
                    .authenticated().requestMatchers(
                        selfApplicationMatcher(
                            POST, "/users/*/applications/*/environments/*/deletionInitiation"))
                    .authenticated()
                    .requestMatchers(selfUserMatcher(PUT, "/users/*/applications"))
                    .authenticated()
                    .requestMatchers(selfUserMatcher(GET, "/users/*/applications"))
                    .authenticated()
                    .requestMatchers(selfUserMatcher(GET, "/users/*/applications/*"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(GET, "/users/*/applications/*/environments"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(PUT, "/users/*/applications/*/environments"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(GET, "/users/*/applications/*/environments/*"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            POST, "/users/*/applications/*/environments/*/ssmparameters"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            PUT, "/users/*/applications/*/environments/*/ssmparameters"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            GET, "/users/*/applications/*/environments/*/ssmparameters"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            GET, "/users/*/applications/*/environments/*/stacks"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            GET, "/users/*/applications/*/environments/*/stacks/*"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            GET, "/users/*/applications/*/environments/*/stacks/*/events"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            GET, "/users/*/applications/*/environments/*/stacks/*/outputs"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            PUT, "/users/*/applications/*/environments/*/config"))
                    .authenticated()
                    .requestMatchers(
                        selfApplicationMatcher(
                            GET, "/users/*/applications/*/environments/*/config"))
                    .authenticated()
                    .requestMatchers(GET, "/gh-repos/*/*/upload-build-uri")
                    .hasRole(GITHUB_APPLICATION.getRole())
                    .requestMatchers(PUT, "/gh-repos/*/*/env-deploys")
                    .hasRole(GITHUB_APPLICATION.getRole())
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

  private RequestMatcher selfUserMatcher(HttpMethod method, String antPath) {
    return new SelfUserMatcher(method, antPath, authenticatedResourceProvider);
  }

  private RequestMatcher selfApplicationMatcher(HttpMethod method, String antPath) {
    return new SelfApplicationMatcher(method, antPath, authenticatedResourceProvider);
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

  private BearerAuthFilter bearerFilter(RequestMatcher requestMatcher) {
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
