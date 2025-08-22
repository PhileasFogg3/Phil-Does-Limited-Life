package org.phileasfogg3.limitedLife.Managers;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.phileasfogg3.limitedLife.LimitedLife;

import java.util.*;

public class BoogeymenManager {
    private Config gameMgr;
    private Config playerData;
    private Config messagesData;

    public BoogeymenManager(Config gameMgr, Config playerData, Config messagesData) {
        this.gameMgr = gameMgr;
        this.playerData = playerData;
        this.messagesData = messagesData;
    }

    public void selectBoogeymen(int boogeymenCount, long drawTime) {
            new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> boogies = new ArrayList<>();
                Random rand = new Random();
                //int boogeymenCount = gameMgr.getData().getInt("boogeymen-settings.boogeymen-count");
                for (int i = 0; i < boogeymenCount; i++) {
                    int randIndex = rand.nextInt(getEligiblePlayers().size());
                    String randomPlayer = getEligiblePlayers().get(randIndex);
                    playerData.getData().getConfigurationSection("players").getKeys(false).forEach((key) -> {
                        if (key.equals(randomPlayer)) {
                            Map<String, Object> playerDataMap = getPlayerValues(key);
                            playerDataMap.put("Boogeyman", true);
                            saveConfig(key, playerDataMap);
                            // Tell the chosen players that they are the Boogeymen
                            UUID uuid = UUID.fromString(key);
                            Player player = Bukkit.getPlayer(uuid);
                            boogies.add(player);
                        }
                    });
                }
                BoogeymenCountdown(boogies);
            }
        }.runTaskLater(LimitedLife.Instance,drawTime * 20L);
    }

    public void cureBoogeymen(Player player) {
        player.sendMessage(ChatColor.GREEN + "You have been cured! You are no longer hostile to other players");
        Map<String, Object> playerDataMap = getPlayerValues(player.getUniqueId().toString());
        playerDataMap.put("Boogeyman", false);
        saveConfig(player.getUniqueId().toString(), playerDataMap);
    }

    public void punishBoogeymen() {
        playerData.getData().getConfigurationSection("players").getKeys(false).forEach((key) -> {
            if (playerData.getData().getBoolean("players." + key + ".Boogeyman")) {
                UUID uuid = UUID.fromString(key);
                Player player = Bukkit.getPlayer(uuid);
                Map<String, Object> playerDataMap = getPlayerValues(player.getUniqueId().toString());

                Long timeLeft = LimitedLife.Instance.playerTimes.get(uuid);

                playerDataMap.put("Time", timeLeft-28800);
                playerDataMap.put("Boogeyman", false);
                saveConfig(player.getUniqueId().toString(), playerDataMap);
                player.sendMessage(ChatColor.RED + "You have failed. You have lost 4 hours of time.");

                TimerManager newTimer = new TimerManager(player, uuid, playerData, gameMgr);
                newTimer.runTaskTimer(LimitedLife.Instance, 20L, 20L); // start after 1s, repeat every 1s
                LimitedLife.Instance.countdowns.put(uuid, newTimer);
            }
        });
    }

    public ArrayList<String> getEligiblePlayers() {
        ArrayList<String> eligiblePlayers = new ArrayList<>();
        playerData.getData().getConfigurationSection("players").getKeys(false).forEach(key -> {
            final boolean isOnline = playerData.getData().getBoolean("players." + key + ".Online");
            final boolean isBoogey = playerData.getData().getBoolean("players." + key + ".Boogeyman");
            final long timeLeft = playerData.getData().getLong("players." + key + ".Time");
            if (isOnline && !isBoogey && timeLeft > 0) {
                eligiblePlayers.add(key);
            }
        });
        // Returns a list of UUIDs that are eligible to be boogeymen
        return eligiblePlayers;
    }

    public void announceBoogeymanDraw(long drawTime) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    onlinePlayers.sendMessage(ChatColor.GREEN + "The Boogeymen are about to be chosen...");
                }
            }
        }.runTaskLater(LimitedLife.Instance,(drawTime-60L) * 20L);
    }

    public void BoogeymenCountdown(List<Player> boogies) {
        List<String> messages = Arrays.asList(
                ChatColor.GREEN + "3",
                ChatColor.YELLOW + "2",
                ChatColor.RED + "1",
                ChatColor.GRAY + "You are..."
        );

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= messages.size()) {
                    // After countdown finishes
                    cancel();

                    // Send special message to the boogeymen
                    boogies.forEach(boogie -> {
                        boogie.sendTitle(ChatColor.RED + "THE Boogeyman!", "");
                        boogie.sendMessage(ChatColor.RED + "You are the Boogeyman and are now hostile to all players. All alliances and friendships are broken. Kill another player and gain an extra hour.");
                        boogie.sendMessage(ChatColor.RED + "If you fail to kill another player before the session ends you will lose 8 hours of time...");
                        boogie.playSound(boogie.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
                    });

                    // Send message to all other players
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers().stream().filter(player -> !boogies.contains(player)).toList()) {
                        onlinePlayer.sendTitle(ChatColor.GREEN + "NOT the Boogeyman!", "");
                        onlinePlayer.sendMessage(ChatColor.GREEN + "You are not the Boogeyman!");
                        onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
                    }
                    return;
                }

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendTitle(messages.get(index), "");
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }

                index++;
            }
        }.runTaskTimer(LimitedLife.Instance, 0L, 40L);
    }

    private Map<String, Object> getPlayerValues(String uuid) {
        return playerData.getData().getConfigurationSection("players." + uuid).getValues(false);
    }

    private void saveConfig(String uuid, Map<String, Object> playerDataMap) {
        // Method to save the playerData.yml file.
        playerData.getData().createSection("players." + uuid, playerDataMap);
        playerData.save();
    }
}
