package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {}
