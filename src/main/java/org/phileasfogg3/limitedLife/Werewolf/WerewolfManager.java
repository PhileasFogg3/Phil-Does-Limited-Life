package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.entity.Player;

import java.util.*;

public class WerewolfManager {

    private Config playerData;
    private Config gameMgr;
    private Config werewolf;

    public final StateManager stateManager;

    public WerewolfManager(Config playerData, Config gameMgr, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.werewolf = werewolf;

        stateManager = new StateManager(werewolf);
        initializeStates();
    }

    private void initializeStates() {
        stateManager.onMorning.add(() -> {
            clearInteractions();
        });
    }

    public void initialisePlayers(Player player) {

        if (!werewolf.getData().contains("players." + player.getUniqueId())) {

            Map<String, Object> playerDataMap = new HashMap<>() {{
                put("Role", "");
                put("Alive", true);
                put("Interactions", new ArrayList<>());
            }};

            saveWerewolfConfig(player.getUniqueId(), playerDataMap);
        }

    }

    public void start() {
        stateManager.start();
    }

    public void end() {
        stateManager.end();
    }

    public void clearInteractions() {

        for (String id : getPlayersUUIDs()) {
            UUID playerID = UUID.fromString(id);
            Map<String, Object> playerDataMap = getPlayerWerewolfValues(playerID);
            playerDataMap.put("Interactions", new ArrayList<>());
            saveWerewolfConfig(playerID, playerDataMap);
        }

    }

    public void logInteraction (Player player1, Player player2) {

        Map<String, Object> player1DataMap = getPlayerWerewolfValues(player1.getUniqueId());
        Map<String, Object> player2DataMap = getPlayerWerewolfValues(player2.getUniqueId());

        List<String> player1List = (List<String>) player1DataMap.get("Interactions");
        List<String> player2List = (List<String>) player2DataMap.get("Interactions");

        // CHECK IF THEY HAD AN INTERACTION FIRST!!!!!!!!!!
        if (!player1List.contains(player2.getName()) && !player2List.contains(player1.getName())) {

            player1List.add(player2.getName());
            player2List.add(player1.getName());

            player1DataMap.put("Interactions", player1List);
            saveWerewolfConfig(player1.getUniqueId(), player1DataMap);

            player2DataMap.put("Interactions", player2List);
            saveWerewolfConfig(player2.getUniqueId(), player2DataMap);
        }

    }

    private Map<String, Object> getPlayerWerewolfValues(UUID playerID) {
        return werewolf.getData().getConfigurationSection("players." + playerID).getValues(false);
    }

    private Set<String> getPlayersUUIDs() {
        return werewolf.getData().getConfigurationSection("players").getKeys(false);
    }

    private void saveWerewolfConfig(UUID playerID, Map<String, Object> playerDataMap) {
        // Method to save the playerData.yml file.
        werewolf.getData().createSection("players." + playerID, playerDataMap);
        werewolf.save();
    }

}
