package api.jcloudify.app.repository;

import api.jcloudify.app.model.PojaVersion;
import java.util.List;

public interface PojaVersionRepository {
  List<PojaVersion> findAll();
}
