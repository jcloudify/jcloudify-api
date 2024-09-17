package api.jcloudify.app.repository.model.workflows;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public interface StateMachine<STATE_ENUM extends Enum<STATE_ENUM>> {
  List<State<STATE_ENUM>> to(State<STATE_ENUM> newState);
}
