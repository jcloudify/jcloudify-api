package api.jcloudify.app.model;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public abstract class StateMachine<STATE_ENUM extends Enum<STATE_ENUM>> {
    private final List<State<STATE_ENUM>> states;

    public abstract List<State<STATE_ENUM>> addState(State<STATE_ENUM> state);

    public State<STATE_ENUM> getLatestState() {
        var list = new ArrayList<>(states);
        list.sort(Comparator.comparing(State::getT));
        return list.getFirst();
    };
}
