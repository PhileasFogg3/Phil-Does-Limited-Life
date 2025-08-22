package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Utils.StateMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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

    private final StateMachine<GameStates> _gameState = new StateMachine<>();
    private final Config wwConfig;
    private BukkitTask task;

    public List<Runnable> onWaiting = new ArrayList<>();
    public List<Runnable> onRoles = new ArrayList<>();
    public List<Runnable> onMorning = new ArrayList<>();
    public List<Runnable> onVoting = new ArrayList<>();
    public List<Runnable> onRoaming = new ArrayList<>();
    public List<Runnable> onNight = new ArrayList<>();
    public List<Runnable> onSleep = new ArrayList<>();
    public List<Runnable> onActions = new ArrayList<>();
    public List<Runnable> onWakeUp = new ArrayList<>();

    public StateManager(Config werewolf) {

        this.wwConfig = werewolf;

        _gameState.addState(GameStates.Waiting, this::waiting);
        _gameState.addState(GameStates.Roles, this::roles).setPostState(this::postRoles);
        _gameState.addState(GameStates.Morning, this::morning);
        _gameState.addState(GameStates.Voting, this::voting);
        _gameState.addState(GameStates.Roaming, this::roaming);
        _gameState.addState(GameStates.Actions, this::actions);
        _gameState.addState(GameStates.Night, this::night);
        _gameState.addState(GameStates.Sleep, this::sleep);
        _gameState.addState(GameStates.WakeUp, this::wakeUp);

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
            if (time < 6000 && time > 0) {
                task.cancel();
                _gameState.setState(GameStates.Morning);
            }
        }, 0, 200);
    }

    private void postRoles() {
        task = Bukkit.getScheduler().runTaskTimer(LimitedLife.Instance, this::loop, 0, 20);
    }

    private void morning() {
        onMorning.forEach(Runnable::run);
    }

    private void voting() {
        onVoting.forEach(Runnable::run);
    }

    private void roaming() {
        onRoaming.forEach(Runnable::run);
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

    private void wakeUp() {
        onWakeUp.forEach(Runnable::run);
    }

    public GameStates getState() {
        return _gameState.getCurrentState();
    }

    public void start() {
        _gameState.setState(GameStates.Waiting);
    }

    public void end() {
        _gameState.setState(GameStates.Disabled);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public long getTime() {
        String worldName = wwConfig.getData().getString("world_name");
        if (worldName == null) worldName = "world";
        return Objects.requireNonNull(Bukkit.getServer().getWorld(worldName)).getTime();
    }

    public void setTime(long tick) {
        String worldName = wwConfig.getData().getString("world_name");
        if (worldName == null) worldName = "world";
        Objects.requireNonNull(Bukkit.getServer().getWorld(worldName)).setTime(tick);
    }

    private void loop() {
        long time = getTime();

        if (tryChange(time, 0L, GameStates.Roles, GameStates.Morning)) return;
        if (tryChange(time, 1000L, GameStates.Morning, GameStates.Voting)) return;
        if (tryChange(time, 4600L, GameStates.Voting, GameStates.Roaming)) return;
        if (tryChange(time, 12000L, GameStates.Roaming, GameStates.Night)) return;
        if (tryChange(time, 13000L, GameStates.Night, GameStates.Sleep)) return;
        if (tryChange(time, 18000L, GameStates.Sleep, GameStates.Actions)) return;
        if (tryChange(time, 21600L, GameStates.Actions, GameStates.WakeUp)) return;
        tryChange(time, 24000L, GameStates.WakeUp, GameStates.Morning);
    }

    private boolean tryChange(long currentTime, long waitTime, GameStates from, GameStates to) {
        if (getState() == from && currentTime >= waitTime) {
            _gameState.setState(to);
            return true;
        }
        return false;
    }

}
