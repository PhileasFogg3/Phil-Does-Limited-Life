package org.phileasfogg3.limitedLife.Utils;

import java.util.HashMap;

public class StateMachine<T> {

    T _currentState = null;
    HashMap<T, Runnable> _states = new HashMap<>();
    Runnable _preState = null;
    Runnable _postState = null;

    public StateMachine<T> addState(T state, Runnable func) {
        _states.put(state, func);
        return this;
    }

    public StateMachine<T> setPreState(Runnable func) {
        _preState = func;
        return this;
    }

    public StateMachine<T> setPostState(Runnable func) {
        _postState = func;
        return this;
    }

    public boolean changeState(T state) {
        if (!_states.containsKey(state)) {
            return false;
        }

        if (_preState != null) {
            _preState.run();
        }

        _states.get(state).run();
        _currentState = state;

        if (_postState != null) {
            _postState.run();
        }

        return true;
    }

    public T getCurrentState() {
        return _currentState;
    }

}
