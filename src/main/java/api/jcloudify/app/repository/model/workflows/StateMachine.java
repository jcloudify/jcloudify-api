package api.jcloudify.app.repository.model.workflows;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public interface StateMachine<T extends State<? extends Enum<?>>> {
  List<T> getStates();

  Optional<T> getLatestState();

  List<T> addState(T state);
}
