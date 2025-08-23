package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Utils.StateMachine.StateMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class StateManager {

    public enum GameStates {
        Disabled,// Nothing happens here
        Waiting, // Initial waiting time (idle)
        Roles,   // Roles are picked here then wait for morning
        Morning, // Time before voting starts (idle)
        Voting,  // Voting lasts till nighttime
        Night,   // Waiting for sleep (idle)
        Sleep,   // Blindness begins to simulate sleep
        Actions, // Time for special actions (1,5 min)
    }

    private final StateMachine<GameStates> _gameState = new StateMachine<>();
    private final Config wwConfig;
    private final String worldName;
    private BukkitTask task;

    public List<Runnable> onWaiting = new ArrayList<>();
    public List<Runnable> onRoles = new ArrayList<>();
    public List<Runnable> onMorning = new ArrayList<>();
    public List<Runnable> onVoting = new ArrayList<>();
    public List<Runnable> onNight = new ArrayList<>();
    public List<Runnable> onSleep = new ArrayList<>();
    public List<Runnable> onActions = new ArrayList<>();
    public List<Runnable> onActionsExit = new ArrayList<>();

    public StateManager(Config werewolf) {

        this.wwConfig = werewolf;

        String worldName = werewolf.getData().getString("world_name");
        if (worldName == null) worldName = "world";
        this.worldName = worldName;

        _gameState.addState(GameStates.Waiting, this::waiting);
        _gameState.addState(GameStates.Roles, this::roles).setExit(this::exitRoles);
        _gameState.addState(GameStates.Morning, this::morning);
        _gameState.addState(GameStates.Voting, this::voting);
        _gameState.addState(GameStates.Actions, this::actions).setExit(this::exitActions);
        _gameState.addState(GameStates.Night, this::night);
        _gameState.addState(GameStates.Sleep, this::sleep);

        _gameState.onStateChanged.add(s -> Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "New state: " + s.toString())); // TEMPORARY
        _gameState.setState(GameStates.Disabled);

    }

    private void waiting() {
        setTime(0);
        onWaiting.forEach(Runnable::run);

        // Change to roles after 5 minutes
        task = Bukkit.getScheduler().runTaskLater(LimitedLife.Instance, () -> {
            _gameState.setState(GameStates.Roles);
        }, 6000);
    }

    private void roles() {
        onRoles.forEach(Runnable::run);

        // Check time every 10 seconds and set to morning on time 0
        task = Bukkit.getScheduler().runTaskTimer(LimitedLife.Instance, () -> {
            long time = getTime();
            if (time < 1000 && time >= 0) {
                task.cancel();
                task = null;
                Bukkit.getScheduler().runTaskLater(LimitedLife.Instance, () -> {
                    _gameState.setState(GameStates.Morning);
                }, 20);
            }
        }, 0, 200);
    }

    private void exitRoles() {
        task = Bukkit.getScheduler().runTaskTimer(LimitedLife.Instance, this::loop, 0, 20);
    }

    private void morning() {
        onMorning.forEach(Runnable::run);
    }

    private void voting() {
        onVoting.forEach(Runnable::run);
    }

    private void night() {
        onNight.forEach(Runnable::run);
    }

    private void sleep() {
        onSleep.forEach(Runnable::run);
    }

    private void actions() {
        onActions.forEach(Runnable::run);
    }

    private void exitActions() {
        onActionsExit.forEach(Runnable::run);
    }

    public void start() {
        _gameState.setState(GameStates.Waiting);
    }

    public void end() {
        _gameState.setState(GameStates.Disabled);
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public long getTime() {
        return Objects.requireNonNull(Bukkit.getServer().getWorld(worldName)).getTime();
    }

    public void setTime(long tick) {
        Objects.requireNonNull(Bukkit.getServer().getWorld(worldName)).setTime(tick);
    }

    private void loop() {
        long time = getTime();

        if (time < 1000) _gameState.setState(GameStates.Morning);
        else if (time < 6000) _gameState.setState(GameStates.Voting);
        else if (time < 13000) _gameState.setState(GameStates.Night);
        else if (time < 22000-40) _gameState.setState(GameStates.Sleep);
        else if (time < 22000) _gameState.setState(GameStates.Actions);
    }
}
