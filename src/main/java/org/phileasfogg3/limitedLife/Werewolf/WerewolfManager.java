package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class WerewolfManager {

    private Config playerData;
    private Config gameMgr;
    private Config werewolf;

    public StateManager stateManager = new StateManager(werewolf);

    public List<String> blankInteractions = new ArrayList<>();

    public WerewolfManager(Config playerData, Config gameMgr, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.werewolf = werewolf;
    }


    public void initialisePlayers(Player player) {

        if (!werewolf.getData().contains("players." + player.getUniqueId())) {

            Map<String, Object> playerDataMap = new HashMap<String, Object>(){{
                put("Role", "");
                put("Alive", true);
                put("Interactions", blankInteractions);
            }};

            saveWerewolfConfig(player, playerDataMap);

        }

    }

    public void start() {
        stateManager.start();
    }

    public void end() {
        stateManager.end();
    }

    public void clearInteractions(Player player) {

        Map<String, Object> playerDataMap = getPlayerWerewolfValues(player);
        playerDataMap.put("Interactions", blankInteractions);
        saveWerewolfConfig(player, playerDataMap);

    }

    public void logInteraction (Player player1, Player player2) {

        Map<String, Object> player1DataMap = getPlayerWerewolfValues(player1);
        Map<String, Object> player2DataMap = getPlayerWerewolfValues(player2);

        List<String> player1List = (List<String>) player1DataMap.get("Interactions");
        List<String> player2List = (List<String>) player2DataMap.get("Interactions");

        // CHECK IF THEY HAD AN INTERACTION FIRST!!!!!!!!!!
        if (!player1List.contains(player2.getName()) && !player2List.contains(player1.getName())) {

            player1List.add(player2.getName());
            player2List.add(player1.getName());

            player1DataMap.put("Interactions", player1List);
            saveWerewolfConfig(player1, player1DataMap);

            player2DataMap.put("Interactions", player2List);
            saveWerewolfConfig(player2, player2DataMap);
        }

    }

    private Map<String, Object> getPlayerWerewolfValues(Player player) {
        return werewolf.getData().getConfigurationSection("players." + player.getUniqueId()).getValues(false);
    }

    private void saveWerewolfConfig(Player player, Map<String, Object> playerDataMap) {
        // Method to save the playerData.yml file.
        werewolf.getData().createSection("players." + player.getUniqueId(), playerDataMap);
        werewolf.save();
    }

}
