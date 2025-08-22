package org.phileasfogg3.limitedLife.Werewolf;

import org.phileasfogg3.limitedLife.Utils.StateMachine;


public class StateManager {

    public enum GameStates {
        Morning, // Time before voting starts (idle)
        Voting,  // Voting lasts for a couple of minutes
        Roaming, // Time for interactions (idle)
        Night,   // Waiting for nighttime (idle)
        Sleep,   // Blindness begins to simulate sleep
        Actions, // Time for special actions
        WakeUp,  // Blindness is removed
    }

    StateMachine<GameStates> _gameState = new StateMachine<>();

    public StateManager() {

        _gameState.addState(GameStates.Morning, this::morning);
        _gameState.addState(GameStates.Voting, this::voting);
        _gameState.addState(GameStates.Roaming, this::roaming);
        _gameState.addState(GameStates.Actions, this::actions);
        _gameState.addState(GameStates.Night, this::night);
        _gameState.addState(GameStates.Sleep, this::sleep);
        _gameState.addState(GameStates.WakeUp, this::wakeUp);

    }

    private void morning() {

    }

    private void voting() {

    }

    private void roaming() {

    }

    private void night() {

    }

    private void sleep() {

    }

    private void actions() {

    }

    private void wakeUp() {

    }

    public GameStates getState() {
        return _gameState.getCurrentState();
    }

}
