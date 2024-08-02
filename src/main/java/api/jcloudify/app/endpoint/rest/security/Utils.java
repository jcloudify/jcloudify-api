package api.jcloudify.app.endpoint.rest.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class Utils {
  public static final String BEARER_PREFIX = "Bearer ";

  public static String getBearerFromHeader(
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
    Object tokenObject = usernamePasswordAuthenticationToken.getCredentials();
    if (!(tokenObject instanceof String token) || !token.startsWith(BEARER_PREFIX)) {
      return null;
    }
    return token.substring(BEARER_PREFIX.length()).trim();
  }
}
