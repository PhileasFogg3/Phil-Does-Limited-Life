package org.phileasfogg3.limitedLife.Utils.StateMachine;

public class State {

    private Runnable _stateFunc;
    private Runnable _preChangeFunc = null;
    private Runnable _postChangeFunc = null;

    public State(Runnable func) {
        _stateFunc = func;
    }

    public State setPreChange(Runnable func) {
        _preChangeFunc = func;
        return this;
    }

    public State setState(Runnable func) {
        _stateFunc = func;
        return this;
    }

    public State setPostChange(Runnable func) {
        _postChangeFunc = func;
        return this;
    }

    public void preChange() {
        if (_preChangeFunc != null) {
            _preChangeFunc.run();
        }
    }

    public void run() {
        _stateFunc.run();
    }

    public void postChange() {
        if (_postChangeFunc != null) {
            _postChangeFunc.run();
        }
    }

}
