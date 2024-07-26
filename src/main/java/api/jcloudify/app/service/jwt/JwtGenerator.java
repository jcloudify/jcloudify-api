package api.jcloudify.app.service.jwt;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.time.Duration;
import java.util.Date;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtGenerator {
  private final PrivateKey rs256privateKey;

  public JwtGenerator(@Value("${app.pem}") String rs256privateKey) {
    Security.addProvider(new BouncyCastleProvider());
    this.rs256privateKey = readPem(rs256privateKey);
  }

  @SneakyThrows
  private PrivateKey readPem(String rs256privateKey) {
    PEMParser pemParser = new PEMParser(new StringReader(rs256privateKey));
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    Object object = pemParser.readObject();
    KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
    return kp.getPrivate();
  }

  public String createJwt(int githubAppId, Duration expiration) {
    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);
    JwtBuilder builder =
        Jwts.builder().issuedAt(now).issuer(String.valueOf(githubAppId)).signWith(rs256privateKey);
    if (expiration.toMillis() > 0) {
      long expMillis = nowMillis + expiration.toMillis();
      Date exp = new Date(expMillis);
      builder.expiration(exp);
    }
    return builder.compact();
  }
}
