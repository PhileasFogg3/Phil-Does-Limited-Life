package org.phileasfogg3.limitedLife.Werewolf;

import org.phileasfogg3.limitedLife.Utils.StateMachine;

import java.util.ArrayList;
import java.util.List;


public class StateManager {

    public enum GameStates {
        Disabled,// Nothing happens here
        Waiting, // Initial waiting time (idle)
        Roles,   // Roles are picked here then wait for morning
        Morning, // Time before voting starts (idle)
        Voting,  // Voting lasts for a couple of minutes
        Roaming, // Time for interactions (idle)
        Night,   // Waiting for nighttime (idle)
        Sleep,   // Blindness begins to simulate sleep
        Actions, // Time for special actions
        WakeUp,  // Blindness is removed
    }

    private boolean started = false;

    StateMachine<GameStates> _gameState = new StateMachine<>();

    public List<Runnable> onWaiting = new ArrayList<>();
    public List<Runnable> onRoles = new ArrayList<>();
    public List<Runnable> onMorning = new ArrayList<>();
    public List<Runnable> onVoting = new ArrayList<>();
    public List<Runnable> onRoaming = new ArrayList<>();
    public List<Runnable> onNight = new ArrayList<>();
    public List<Runnable> onSleep = new ArrayList<>();
    public List<Runnable> onActions = new ArrayList<>();
    public List<Runnable> onWakeUp = new ArrayList<>();

    public StateManager() {

        _gameState.addState(GameStates.Waiting, this::waiting);
        _gameState.addState(GameStates.Roles, this::roles);
        _gameState.addState(GameStates.Morning, this::morning);
        _gameState.addState(GameStates.Voting, this::voting);
        _gameState.addState(GameStates.Roaming, this::roaming);
        _gameState.addState(GameStates.Actions, this::actions);
        _gameState.addState(GameStates.Night, this::night);
        _gameState.addState(GameStates.Sleep, this::sleep);
        _gameState.addState(GameStates.WakeUp, this::wakeUp);

        _gameState.changeState(GameStates.Disabled);

    }

    public void waiting() {
        if (started) onWaiting.forEach(Runnable::run);
    }

    public void roles() {
        if (started) onRoles.forEach(Runnable::run);
    }

    private void morning() {
        if (started) onMorning.forEach(Runnable::run);
    }

    private void voting() {
        if (started) onVoting.forEach(Runnable::run);
    }

    private void roaming() {
        if (started) onRoaming.forEach(Runnable::run);
    }

    private void night() {
        if (started) onNight.forEach(Runnable::run);
    }

    private void sleep() {
        if (started) onSleep.forEach(Runnable::run);
    }

    private void actions() {
        if (started) onActions.forEach(Runnable::run);
    }

    private void wakeUp() {
        if (started) onWakeUp.forEach(Runnable::run);
    }

    public GameStates getState() {
        return _gameState.getCurrentState();
    }

    public void start() {
        started = true;
    }

    public void end() {
        started = false;
    }

}
