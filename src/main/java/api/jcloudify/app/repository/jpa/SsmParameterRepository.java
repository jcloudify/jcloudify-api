package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.SsmParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SsmParameterRepository extends JpaRepository<SsmParameter, String> {
}
