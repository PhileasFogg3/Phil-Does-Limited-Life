package org.phileasfogg3.limitedLife.Listeners;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Managers.BoogeymenManager;
import org.phileasfogg3.limitedLife.Managers.PlayerNameManager;
import org.phileasfogg3.limitedLife.Managers.TimerManager;
import org.phileasfogg3.limitedLife.Werewolf.WerewolfManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerListener implements Listener {

    private Config playerData;
    private Config gameMgr;
    private Config messagesData;
    private Config werewolf;

    public PlayerListener(Config playerData, Config gameMgr, Config messagesData, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.messagesData = messagesData;
        this.werewolf = werewolf;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!playerData.getData().contains("players." + player.getUniqueId())) {
            // Functionality for when a unique player joins

            Map<String, Object> playerDataMap = new HashMap<String, Object>(){{
                put("Boogeyman", false);
                put("Time", 86400L);
                put("Online", true);
                put("Life", 4);
            }};

            saveConfig(player, playerDataMap);

        } else {
            // Functionality for when a non-unique player joins

            Map<String, Object> playerDataMap = getPlayerValues(player);
            playerDataMap.put("Online", true);
            saveConfig(player, playerDataMap);

            PlayerNameManager PNM = new PlayerNameManager(playerData, gameMgr);
            PNM.teamManager(player, playerData.getData().getInt("players." + player.getUniqueId() + ".Life"));

        }

        // Functionality for when all players join

        var uuid = player.getUniqueId();
        long timeLeft = playerData.getData().getLong("players." + uuid + ".Time");

        PlayerNameManager PNM = new PlayerNameManager(playerData, gameMgr);
        PNM.checkUpdate(timeLeft, player);

        if (gameMgr.getData().getBoolean("session-active") && !gameMgr.getData().getBoolean("break-active")) {
            LimitedLife.Instance.playerTimes.put(uuid, timeLeft);

            // Start countdown only if they have time left
            if (playerData.getData().getLong("players." + uuid + ".Time") >= 0L) {
                TimerManager timer = new TimerManager(player, uuid, playerData, gameMgr);
                timer.runTaskTimer(LimitedLife.Instance, 20L, 20L); // start after 1s, repeat every 1s
                LimitedLife.Instance.countdowns.put(uuid, timer);
            }
        }

        // Relevant to Werewolf
        WerewolfManager WM = new WerewolfManager(playerData, gameMgr, werewolf);
        WM.initialisePlayers(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        var uuid = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();
        Long timeLeft = LimitedLife.Instance.playerTimes.get(uuid);

        if (timeLeft == null) {
            timeLeft = playerData.getData().getLong("players." + uuid + ".Time");
        }

        System.out.println(timeLeft);

        Map<String, Object> playerDataMap = getPlayerValues(player);
        playerDataMap.put("Online", false);
        playerDataMap.put("Time", timeLeft);
        saveConfig(player, playerDataMap);

        // Cancel task if running
        TimerManager timer = LimitedLife.Instance.countdowns.remove(uuid);
        if (timer != null) {
            timer.cancel();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (!gameMgr.getData().getBoolean("session-active") || gameMgr.getData().getBoolean("break-active")) {
            return;
        }
        Player victim = e.getEntity();
        if (victim.getKiller() != null) {
            // Victim was killed by a player

            if (victim.getKiller() == victim) {
                deathHandler(victim, null);
                Bukkit.broadcastMessage(ChatColor.GRAY + "So... I have a story to tell you... " + victim.getName() + " decided it would be a good idea for them to kill themselves. Why? I have no idea. " + victim.getName() + " I think you should explain yourself to the whole server.");
            } else {
                deathHandler(victim, victim.getKiller());
                System.out.println(victim.getName() + " has been killed by a player 1");
            }

        } else {
            deathHandler(victim, null);

            System.out.println(victim.getName() + " has died to the world 1");

        }

        deathLogger(victim, e.getDeathMessage());

    }

    public void deathHandler(Player victim, Player killer) {

        if (killer != null && playerData.getData().getBoolean("players." + killer.getUniqueId() + ".Boogeyman")) {

            // Logic if killed by BoogeyMan

            subtractTime(victim, 7200L);
            addTime(killer, 3600L);

            System.out.println(victim.getName() + " has been killed by the Boogeyman 2");

            BoogeymenManager BM = new BoogeymenManager(gameMgr, playerData, messagesData);
            BM.cureBoogeymen(killer);

        } else if (killer != null && !playerData.getData().getBoolean("players." + killer.getUniqueId() + ".Boogeyman")) {

            // Logic if killed by player but not boogeyman

            subtractTime(victim, 3600L);
            addTime(killer, 1800L);

            System.out.println(victim.getName() + " has been killed by a player 2");

        } else {

            // Logic if player died to the world

            subtractTime(victim, 3600L);

        }

    }

    public void subtractTime(Player player, Long time) {
        Map<String, Object> playerMap = getPlayerValues(player);
        Long timeLeft = LimitedLife.Instance.playerTimes.get(player.getUniqueId());

        if (timeLeft == null) {
            return;
        }

        Long newTime = timeLeft - time;

        if (newTime <= 0) {
            newTime = 0L;
        }

        playerMap.put("Time", newTime);
        saveConfig(player, playerMap);

        LimitedLife.Instance.playerTimes.put(player.getUniqueId(), newTime);

        // Elimination check
        if (newTime <= 0) {
            System.out.println(player.getName() + " has been eliminated");

        }

        // Display correct time deducted
        if (time == 1800L) {
            player.sendTitle(ChatColor.RED + "-30 Minutes", "");
        } else if (time % 3600 == 0) {
            long hours = time / 3600;
            player.sendTitle(ChatColor.RED + "-" + hours + (hours == 1 ? " Hour" : " Hours"), "");
        } else {
            long minutes = time / 60;
            player.sendTitle(ChatColor.RED + "-" + minutes + " Minutes", "");
        }

        PlayerNameManager PNM = new PlayerNameManager(playerData, gameMgr);
        PNM.checkUpdate(newTime, player);
    }

    public void addTime(Player player, Long time) {
        Map<String, Object> playerMap = getPlayerValues(player);
        Long timeLeft = LimitedLife.Instance.playerTimes.get(player.getUniqueId());

        if (timeLeft == null) {
            return;
        }

        Long newTime = timeLeft + time;

        playerMap.put("Time", newTime);
        saveConfig(player, playerMap);

        LimitedLife.Instance.playerTimes.put(player.getUniqueId(), newTime);

        // Display appropriate title
        if (time == 1800) { // 30 minutes in seconds
            player.sendTitle(ChatColor.GREEN + "+30 Minutes", "");
        } else if (time % 3600 == 0) {
            long hours = time / 3600;
            player.sendTitle(ChatColor.GREEN + "+" + hours + (hours == 1 ? " Hour" : " Hours"), "");
        } else {
            long minutes = time / 60;
            player.sendTitle(ChatColor.GREEN + "+" + minutes + " Minutes", "");
        }

        PlayerNameManager PNM = new PlayerNameManager(playerData, gameMgr);
        PNM.checkUpdate(newTime, player);
    }


    public void deathLogger(Player player, String deathMessage) {
        Map<String, Object> playerMap = getPlayerValues(player);

        List<String> deathList = playerData.getData().getStringList("players." + player.getUniqueId() + ".Deaths");

        deathList.add(deathMessage);

        playerMap.put("Deaths", deathList);
        saveConfig(player, playerMap);
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