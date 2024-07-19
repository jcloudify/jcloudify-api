package api.jcloudify.app.repository.impl;

import static java.util.EnumSet.allOf;

import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.repository.PojaVersionRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PojaVersionRepositoryImpl implements PojaVersionRepository {

  private static final List<PojaVersion> ALL_POJA_VERSIONS =
      allOf(PojaVersion.class).stream().toList();

  @Override
  public List<PojaVersion> findAll() {
    return ALL_POJA_VERSIONS;
  }
}
