package api.jcloudify.app.endpoint.rest.security;

import api.jcloudify.app.model.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConf {

  private final HandlerExceptionResolver exceptionResolver;

  public SecurityConf(
      // InternalToExternalErrorHandler behind
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.exceptionResolver = exceptionResolver;
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
        .authorizeHttpRequests(
            (authorize) ->
                authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/ping")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/health/db")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/health/bucket")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/health/event")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/health/email")
                    .permitAll()
                    .requestMatchers("/**")
                    .denyAll())
        // disable superfluous protections
        // Eg if all clients are non-browser then no csrf
        // https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/csrf.html,
        // Sec 13.3
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
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
}
