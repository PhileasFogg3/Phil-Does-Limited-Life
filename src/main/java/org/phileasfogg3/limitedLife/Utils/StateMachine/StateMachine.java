package org.phileasfogg3.limitedLife.Utils.StateMachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class StateMachine<T> {

    public List<Consumer<T>> onStateChanged = new ArrayList<>();

    private final HashMap<T, State> _states = new HashMap<>();
    private T _currentState = null;

    public State addState(T key, Runnable func) {
        _states.put(key, new State(func));
        return _states.get(key);
    }

    public boolean setState(T key) {
        if (_currentState == key) {
            return false;
        }

        if (!_states.containsKey(key)) {
            return false;
        }

        State state = _states.get(key);

        state.preChange();
        state.run();

        _currentState = key;
        onStateChanged.forEach(c -> c.accept(key));

        state.postChange();

        return true;
    }

    public T getCurrentState() {
        return _currentState;
    }

}
