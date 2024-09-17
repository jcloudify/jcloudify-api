package api.jcloudify.app.repository.model.workflows;

import org.springframework.stereotype.Component;

@Component
public interface StateMachine<STATE_ENUM extends Enum<STATE_ENUM>> {
    State<STATE_ENUM> to(State<STATE_ENUM> newState);
}
