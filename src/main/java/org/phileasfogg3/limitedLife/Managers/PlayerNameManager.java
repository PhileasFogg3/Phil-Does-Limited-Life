package org.phileasfogg3.limitedLife.Managers;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerNameManager {

    private Config playerData;
    private Config gameMgr;

    public PlayerNameManager(Config playerData, Config gameMgr) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
    }

    public void checkUpdate(Long time, Player player) {

        int expectedLife;
        int newLife;

        if (time >= 57600) {

            expectedLife = 3;

        } else if (time >= 28800) {

            expectedLife = 2;

        } else if (time > 0) {

            expectedLife = 1;

        } else {

            expectedLife = 0;

        }

        int actualLife = playerData.getData().getInt("players." + player.getUniqueId() + ".Life");

        if ( actualLife != expectedLife) {

            Map<String, Object> playerDataMap = getPlayerValues(player);

            playerDataMap.put("Life", expectedLife);
            saveConfig(player, playerDataMap);


            // Call Methods for changing colour

            teamManager(player, expectedLife);

        }

        setTABListTime(player, updateTimeDisplay(time), colorFromTime(time));

    }

    public void teamManager(Player player, int life) {

        TeamsManager tM = new TeamsManager(gameMgr);
        Map<Integer, String> teamsMap = tM.getTeamsFromYML();

        playerData.getData().getConfigurationSection("players.").getKeys(false).forEach(key -> {

            if (player.getUniqueId().toString().equals(key)) {

                String colour = "";

                switch (life) {

                    case 3:
                        colour = teamsMap.get(3);
                        player.setGameMode(GameMode.SURVIVAL);
                        System.out.println(player.getName() + " has joined the team " + teamsMap.get(3));
                        break;
                    case 2:
                        colour = teamsMap.get(2);
                        player.setGameMode(GameMode.SURVIVAL);
                        System.out.println(player.getName() + " has joined the team " + teamsMap.get(2));
                        break;
                    case 1:
                        colour = teamsMap.get(1);
                        player.setGameMode(GameMode.SURVIVAL);
                        System.out.println(player.getName() + " has joined the team " + teamsMap.get(1));
                        break;
                    default:
                        colour = teamsMap.get(0);
                        player.setGameMode(GameMode.SPECTATOR);
                        System.out.println(player.getName() + " has joined the team " + teamsMap.get(0));

                }

                changeName(player, colour);

            }

        });

    }

    public String updateTimeDisplay(Long time) {

        String timeDisplay = String.format("%02d:%02d:%02d",
                TimeUnit.SECONDS.toHours(time),
                TimeUnit.SECONDS.toMinutes(time) % 60,
                time % 60);

        return getColor(colorFromTime(time)) + timeDisplay;
    }

    public void changeName(Player player, String colour) {
        // Gets the scoreboard
        Scoreboard sm = Bukkit.getScoreboardManager().getMainScoreboard();
        // Gets the chat colour object based on the String colour.
        // Gives the player the correct colour in chat and tablist.
        System.out.println("Set the display name of " + player.getName() + " to " + getColor(colour) + player.getName() + ChatColor.RESET);
        player.setDisplayName(getColor(colour) + player.getName() + ChatColor.RESET);
        player.setPlayerListName(getColor(colour) + player.getName() + ChatColor.RESET);
        // Adds player to the correct team.
        sm.getTeam(colour).addEntry(player.getName());
    }

    public void setTABListTime (Player player, String string, String colour) {

        player.setPlayerListName(getColor(colour) + player.getName() + ChatColor.GRAY + " [" + string + ChatColor.GRAY + "]" + ChatColor.RESET);
        player.setDisplayName(getColor(colour) + player.getName() + ChatColor.RESET);

    }

    public ChatColor getColor(String colour) {

        ChatColor color = ChatColor.valueOf(colour.toUpperCase());

        return color;
    }

    public String colorFromTime (Long time) {

        TeamsManager tM = new TeamsManager(gameMgr);
        Map<Integer, String> teamsMap = tM.getTeamsFromYML();
        String colour;

        if (time >= 57600) {
            colour = teamsMap.get(3);
        } else if (time >= 28800) {
            colour = teamsMap.get(2);
        } else if (time >= 0) {
            colour = teamsMap.get(1);
        } else {
            colour = teamsMap.get(0);
        }

        return colour;
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