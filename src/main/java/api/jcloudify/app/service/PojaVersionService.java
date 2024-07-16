package api.jcloudify.app.service;

import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.repository.PojaVersionRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PojaVersionService {
  private final PojaVersionRepository repository;

  public List<PojaVersion> findAll() {
    return repository.findAll();
  }
}
