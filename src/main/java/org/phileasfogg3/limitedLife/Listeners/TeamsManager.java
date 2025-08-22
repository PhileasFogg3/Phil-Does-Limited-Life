package org.phileasfogg3.limitedLife.Listeners;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TeamsManager {

    private Config gameMgr;

    public TeamsManager(Config gameMgr) {this.gameMgr = gameMgr;}

    public Map<Integer, String> getTeamsFromYML() {
        Map<Integer, String> teamsFromYML = new HashMap<Integer, String>();
        gameMgr.getData().getConfigurationSection("teams").getKeys(false).forEach(key -> {
            int lives = Integer.parseInt(key);
            String teamName = gameMgr.getData().getString("teams." + key);
            teamsFromYML.put(lives, teamName);
        });
        return teamsFromYML;
    }

    public ArrayList<String> getTeamsOnServer() {
        ArrayList<String> teamsOnServer = new ArrayList<>();
        Scoreboard sm = Bukkit.getScoreboardManager().getMainScoreboard();
        sm.getTeams().forEach(team -> teamsOnServer.add(team.getName()));
        return teamsOnServer;
    }

}
