package api.poja.app.conf;

import api.poja.app.PojaGenerated;
import org.springframework.test.context.DynamicPropertyRegistry;

@PojaGenerated
public class EmailConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.ses.source", () -> "dummy-ses-source");
  }
}
