package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class RoleManager {

    private Config playerData;
    private Config gameMgr;
    private Config werewolf;

    public RoleManager(Config playerData, Config gameMgr, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.werewolf = werewolf;
    }

    public void assignRoles() {

        selectWerewolves();
        selectDoctor();
        selectTruthSeeker();

        assignVillagers();

    }

    public void selectWerewolves() {

        List<Player> werewolves = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < 2; i++) {
            int randIndex = rand.nextInt(getEligiblePlayers().size());
            String randomPlayer = getEligiblePlayers().get(randIndex);

            werewolf.getData().getConfigurationSection("players").getKeys(false).forEach((key) -> {
                if (key.equals(randomPlayer)) {
                    Map<String, Object> playerDataMap = getPlayerWerewolfValues(key);
                    playerDataMap.put("Role", "Werewolf");
                    saveWerewolfConfig(key, playerDataMap);

                    UUID uuid = UUID.fromString(key);
                    Player player = Bukkit.getPlayer(uuid);

                    werewolves.add(player);

                }
            });
        }

        // Tell the players they are werewolves here.

    }

    public void selectDoctor() {

        Random rand = new Random();
        for (int i = 0; i < 1; i++) {
            int randIndex = rand.nextInt(getEligiblePlayers().size());
            String randomPlayer = getEligiblePlayers().get(randIndex);

            werewolf.getData().getConfigurationSection("players").getKeys(false).forEach((key) -> {
                if (key.equals(randomPlayer)) {
                    Map<String, Object> playerDataMap = getPlayerWerewolfValues(key);
                    playerDataMap.put("Role", "Doctor");
                    saveWerewolfConfig(key, playerDataMap);

                    // Tell player they are the Doctor here

                }
            });
        }

    }

    public void selectTruthSeeker() {

        Random rand = new Random();
        for (int i = 0; i < 1; i++) {
            int randIndex = rand.nextInt(getEligiblePlayers().size());
            String randomPlayer = getEligiblePlayers().get(randIndex);

            werewolf.getData().getConfigurationSection("players").getKeys(false).forEach((key) -> {
                if (key.equals(randomPlayer)) {
                    Map<String, Object> playerDataMap = getPlayerWerewolfValues(key);
                    playerDataMap.put("Role", "TruthSeeker");
                    saveWerewolfConfig(key, playerDataMap);

                    // Tell player they are the TruthSeeker here

                }
            });
        }

    }

    public ArrayList<String> getEligiblePlayers() {
        ArrayList<String> eligiblePlayers = new ArrayList<>();
        werewolf.getData().getConfigurationSection("players").getKeys(false).forEach(key -> {
            final String role = werewolf.getData().getString("players." + key + ".Role");
            if (role.equals("")) {
                eligiblePlayers.add(key);
            }
        });
        // Returns a list of UUIDs that are eligible to have a specific role.
        return eligiblePlayers;
    }

    public void assignVillagers() {

        List<Player> VillagerList = new ArrayList<>();

        werewolf.getData().getConfigurationSection("players.").getKeys(false).forEach((key) -> {
            final String role = werewolf.getData().getString("players." + key + ".Role");
            if (role.equals("")) {
                Map<String, Object> playerDataMap = getPlayerWerewolfValues(key);
                playerDataMap.put("Role", "Villager");
                saveWerewolfConfig(key, playerDataMap);

                UUID uuid = UUID.fromString(key);
                Player player = Bukkit.getPlayer(uuid);

                VillagerList.add(player);
            }
        });

        // Tell the villagers they are villagers here

    }

    private Map<String, Object> getPlayerWerewolfValues(String uuid) {
        return werewolf.getData().getConfigurationSection("players." + uuid).getValues(false);
    }

    private void saveWerewolfConfig(String uuid, Map<String, Object> playerDataMap) {
        // Method to save the playerData.yml file.
        werewolf.getData().createSection("players." + uuid, playerDataMap);
        werewolf.save();
    }


}
