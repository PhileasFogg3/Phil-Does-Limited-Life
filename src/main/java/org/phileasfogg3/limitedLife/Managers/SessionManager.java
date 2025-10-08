package org.phileasfogg3.limitedLife.Managers;

import net.md_5.bungee.api.ChatColor;
import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.phileasfogg3.limitedLife.LimitedLife;

import java.util.Map;

public class SessionManager {

    private final Config gameMgr;
    private final Config playerData;
    private Config messagesData;

    public SessionManager(Config gameMgr, Config playerData, Config messagesData) {
        this.gameMgr = gameMgr;
        this.playerData = playerData;
        this.messagesData = messagesData;
    }

    public void sessionTimeInitializer() {

        int sessionLength = gameMgr.getData().getInt("session-timings.session-length");
        boolean sessionBreak = gameMgr.getData().getBoolean("session-timings.session-break"); // Set true or false in gameManager.yml Do you want a break, or not?
        int sessionBreakLength = gameMgr.getData().getInt("session-timings.session-break-length");
        int sessionHalf = sessionLength / 2;

        if (sessionHalf % 60 !=0) {
            sessionHalf += (60-sessionHalf % 60); // Rounding up to nearest 60 if not already  multiple of 60.
        }
        if (sessionBreakLength % 60 !=0) {
            sessionBreakLength += (60- sessionBreakLength % 60); // Rounding up to nearest 60 if not already a multiple of 60.
        }
        if (sessionBreak) {
            // If there is a break
            int finalSessionBreakLength = sessionBreakLength;
            int finalSessionHalf = sessionHalf;
            // Counts down the first half of the session, until the break.
            sessionTimer(new int[]{sessionHalf}, "Time left until the break: ", 1, () -> // 1 is before break
                    // Counts down the time left of the break.
                    sessionTimer(new int[]{finalSessionBreakLength}, "Time left of the break: ", 2, () -> // 2 is during break
                            // Counts down the second half of the session, until the session's end.
                            sessionTimer(new int[]{finalSessionHalf}, "Time left of the session: ", 3, null) // 3 is after break
                    )
            );
        } else {
            // If there is no break
            sessionTimer(new int[]{sessionLength}, "Time left of the session: ", 3, null);
        }
    }

    public void resumeInitializer(int time, int id) {

        if (id == 1) {
            sessionTimer(new int[]{time}, "Time left until the break: ", 1, () ->
                    // Counts down the time left of the break.
                    sessionTimer(new int[]{gameMgr.getData().getInt("session-timings.session-break-length")}, "Time left of the break: ", 2, () ->
                            // Counts down the second half of the session, until the session's end.
                            sessionTimer(new int[]{gameMgr.getData().getInt("session-timings.session-length")/2}, "Time left of the session: ", 3, null)
                    )
            );
        } else if (id == 2) {
            sessionTimer(new int[]{time}, "Time left of the break: ", 2, () ->
                    // Counts down the time left of the break.
                    sessionTimer(new int[]{gameMgr.getData().getInt("session-timings.session-length")/2}, "Time left of the session: ", 3, null));
            // Counts down the second half of the session, until the session's end.
        } else if (id == 3) {
            sessionTimer(new int[]{time}, "Time left of the session: ", 3, null);
            // Counts down the time left of the break.
        }
    }

    public void sessionTimer(final int[] time, String tabListMessage, int actionID, Runnable onComplete) {
        // Updates yml and tablist
        sessionProgressUpdater(actionID, time[0]);
        updateTabList(tabListMessage, time[0]);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (time[0] > 0) {
                    // Executes this code if there is more than 1 minute left of the session portion.
                    // Subtracts a minute from the session timer
                    time[0] -= 60;
                    // Updates yml and tablist
                    sessionProgressUpdater(actionID, time[0]);
                    updateTabList(tabListMessage, time[0]);
                    if (time[0] <= 0) {
                        // Executes this code if there is no time left of the session portion.
                        this.cancel();
                        // Requires confirmation to proceed
                        gameMgr.getData().set("confirmation.required", true);
                        gameMgr.save();
                        for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                            // Sends confirmation prompt to all online admins.
                            if (onlinePlayers.hasPermission("petlife.admin")) {
                                onlinePlayers.sendMessage("The session requires confirmation before it can proceed.");
                            }
                        }
                        // Waits for the session progression to be confirmed.
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (gameMgr.getData().getInt("confirmation.value") == 1) {
                                    // Executes this code if the session progression has been confirmed.
                                    this.cancel();
                                    gameMgr.getData().set("confirmation.value", 0);
                                    gameMgr.getData().set("confirmation.required", false);
                                    gameMgr.save();
                                    if (onComplete != null) {
                                        onComplete.run();
                                    }
                                    sessionAction(actionID);
                                }
                            }
                        }.runTaskTimer(LimitedLife.Instance, 0L, 20L);
                    }
                }
            }
        }.runTaskTimer(LimitedLife.Instance,1200L, 1200L);
    }

    public void sessionAction(int id) {
        switch (id) {
            case 1:
                // Do this after first half of the session
                gameMgr.getData().set("session-information.first-half-progress", -1);
                gameMgr.getData().set("break-active", true);
                gameMgr.save();

                stopCountdown();

                break;
            case 2:
                // Do this when the break ends
                gameMgr.getData().set("session-information.break-progress", -1);
                gameMgr.getData().set("break-active", false);
                gameMgr.save();

                startCountdown();

                break;
            case 3:
                // Do this when the second half of the session ends (session should be over)
                gameMgr.getData().set("session-information.second-half-progress", -1);
                gameMgr.getData().set("session-active", false);
                gameMgr.save();
                // Punish Boogeymen that have failed
                BoogeymenManager BM = new BoogeymenManager(gameMgr, playerData, messagesData);
                BM.punishBoogeymen();
                stopCountdown();
                // Send message to players - session over
                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    onlinePlayers.sendMessage(ChatColor.RED + "The session is now over");
                }
                break;
        }
    }

    public void sessionProgressUpdater(int id, int time) {
        // Updates the yml file appropriately so sessions can be resumed in the event of an unsafe crash.
        switch (id) {
            case 1:
                gameMgr.getData().set("session-information.first-half-progress", time);
                break;
            case 2:
                gameMgr.getData().set("session-information.break-progress", time);
                break;
            case 3:
                gameMgr.getData().set("session-information.second-half-progress", time);
                break;
        }
        gameMgr.save();
    }

    public void updateTabList(String body, int time) {
        // Updates the tablist
        String suffix;
        // Logic to make the suffix of the message singular when necessary (when 1 minute remains)
        if (time > 60 || time <=0) {
            suffix = " minutes";
        } else {
            suffix = " minute";
        }
        for (Player onlinePlayers : Bukkit.getServer().getOnlinePlayers()) {
            onlinePlayers.setPlayerListFooter(body + time/60 + suffix); // Converts time to minutes.
        }
    }

    public void sessionStart(int oldSessionNumber) {
        int newSessionNumber = oldSessionNumber + 1;
        gameMgr.getData().set("session-information.session-number", newSessionNumber);
        gameMgr.getData().set("session-active", true);
        gameMgr.save();
        sessionTimeInitializer();
        // Picks boogeymen if there is at least 1 boogey in the config file.
        if (gameMgr.getData().getInt("boogeymen-settings.boogeymen-count") != 0) {
            BoogeymenManager bM = new BoogeymenManager(gameMgr, playerData, messagesData);
            bM.selectBoogeymen(gameMgr.getData().getInt("boogeymen-settings.boogeymen-count"), gameMgr.getData().getInt("boogeymen-settings.boogeymen-draw-time"));
            bM.announceBoogeymanDraw(gameMgr.getData().getInt("boogeymen-settings.boogeymen-draw-time"));
        }
        // Do we want to play with double life rules?
        if (gameMgr.getData().getBoolean("specials.DoubleLife.enabled")) {
            SoulmatesManager SM = new SoulmatesManager(playerData);
            SM.selectSoulmates(gameMgr.getData().getLong("specials.DoubleLife.soulmates-draw-time"));
            SM.announceSoulmates(gameMgr.getData().getInt("specials.DoubleLife.soulmates-draw-time"));
        }
    }

    public void startCountdown() {
        for (Player onlinePlayers : Bukkit.getServer().getOnlinePlayers()) {

            startCountdown(onlinePlayers);

        }
    }

    public void startCountdown(Player player) {

        LimitedLife.Instance.playerTimes.put(player.getUniqueId(), playerData.getData().getLong("players." + player.getUniqueId() + ".Time"));
        TimerManager timer = new TimerManager(player, player.getUniqueId(), playerData, gameMgr);
        timer.runTaskTimer(LimitedLife.Instance, 20L, 20L);
        LimitedLife.Instance.countdowns.put(player.getUniqueId(), timer);

    }

    public void stopCountdown() {
        for (Player onlinePlayers : Bukkit.getServer().getOnlinePlayers()) {

            Long timeLeft = LimitedLife.Instance.playerTimes.get(onlinePlayers.getUniqueId());
            Map<String, Object> playerDataMap = getPlayerValues(onlinePlayers);
            playerDataMap.put("Time", timeLeft);
            saveConfig(onlinePlayers, playerDataMap);

            TimerManager timer = LimitedLife.Instance.countdowns.remove(onlinePlayers.getUniqueId());
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    private Map<String, Object> getPlayerValues(Player player) {
        return playerData.getData().getConfigurationSection("players." + player.getUniqueId()).getValues(false);
    }

    private void saveConfig(Player player, Map<String, Object> playerDataMap) {
        // Method to save the playerData.yml file.
        playerData.getData().createSection("players." + player.getUniqueId(), playerDataMap);
        playerData.save();
    }
}