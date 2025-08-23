package org.phileasfogg3.limitedLife.Utils.StateMachine;

public class State {

    private Runnable _stateFunc;
    private Runnable _enterFunc = null;
    private Runnable _exitFunc = null;

    public State(Runnable func) {
        _stateFunc = func;
    }

    public State setEnter(Runnable func) {
        _enterFunc = func;
        return this;
    }

    public State setFunc(Runnable func) {
        _stateFunc = func;
        return this;
    }

    public State setExit(Runnable func) {
        _exitFunc = func;
        return this;
    }

    public void start() {
        if (_enterFunc != null) {
            _enterFunc.run();
        }
        _stateFunc.run();
    }

    public void end() {
        if (_exitFunc != null) {
            _exitFunc.run();
        }
    }

}
