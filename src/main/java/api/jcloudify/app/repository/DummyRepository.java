package api.jcloudify.app.repository;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.repository.model.Dummy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@PojaGenerated
@Repository
public interface DummyRepository extends JpaRepository<Dummy, String> {

  @Override
  List<Dummy> findAll();
}
