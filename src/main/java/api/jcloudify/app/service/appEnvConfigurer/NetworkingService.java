package api.jcloudify.app.service.appEnvConfigurer;

import api.jcloudify.app.model.pojaConf.NetworkingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class NetworkingService {
  private final NetworkingConfig networkingConfig;

  @SneakyThrows
  public NetworkingService(
      @Value("${apps.envs.networking}") String networkingConfig, ObjectMapper om) {
    this.networkingConfig = om.readValue(networkingConfig, NetworkingConfig.class);
  }
}
