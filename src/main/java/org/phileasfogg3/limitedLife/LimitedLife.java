package org.phileasfogg3.limitedLife;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.phileasfogg3.limitedLife.Commands.LimitedLifeAdminCommand;
import org.phileasfogg3.limitedLife.Commands.LimitedLifeCommand;
import org.phileasfogg3.limitedLife.Listeners.MobListener;
import org.phileasfogg3.limitedLife.Listeners.PlayerListener;
import org.phileasfogg3.limitedLife.Listeners.RecipieViewer;
import org.phileasfogg3.limitedLife.Managers.RecipesManager;
import org.phileasfogg3.limitedLife.Managers.TeamsManager;
import org.phileasfogg3.limitedLife.Managers.TimerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LimitedLife extends JavaPlugin {

    public static LimitedLife Instance;

    public HashMap<UUID, Long> playerTimes = new HashMap<>();
    public HashMap<UUID, TimerManager> countdowns = new HashMap<>();
    public Map<UUID, Boolean> timerDisplayToggle = new HashMap<>();


    Config gameMgr = new Config(this, "gameManager.yml");
    Config playerData = new Config(this, "playerData.yml");
    Config messagesData = new Config(this, "messages.yml");
    Config werewolf = new Config(this, "werewolf.yml");

    @Override
    public void onEnable() {
        // Plugin startup logic
        Instance = this;

        createTeams();
        registerRecipes();
        registerEvents();
        registerCommands();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        deleteTeams();
    }

    public void registerEvents() {

        getServer().getPluginManager().registerEvents(new PlayerListener(playerData, gameMgr, messagesData, werewolf), this);
        getServer().getPluginManager().registerEvents(new RecipieViewer(), this);
        getServer().getPluginManager().registerEvents(new MobListener(gameMgr), this);

    }

    public void registerCommands() {
        LimitedLifeCommand limitedLifeCommand = new LimitedLifeCommand(playerData, gameMgr, messagesData);
        getCommand("limitedlife").setExecutor(limitedLifeCommand);
        getCommand("limitedlife").setTabCompleter(limitedLifeCommand);

        LimitedLifeAdminCommand adminCommand = new LimitedLifeAdminCommand(playerData, gameMgr, messagesData, werewolf);
        getCommand("limitedlifeadmin").setExecutor(adminCommand);
        getCommand("limitedlifeadmin").setTabCompleter(adminCommand);
    }


    public void registerRecipes() {
        RecipesManager RM = new RecipesManager();
        RM.tntRecipe();
        RM.spawnerRecipe();
        RM.nameTagRecipe();
        RM.saddleRecipe();
    }

    public void createTeams() {
        // Creates teams that correspond to colours in the game but only if it does not already exist.
        TeamsManager tM = new TeamsManager(gameMgr);
        Scoreboard sm = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Map.Entry<Integer, String> entry : tM.getTeamsFromYML().entrySet()) {
            String teamName = entry.getValue();
            if (!tM.getTeamsOnServer().contains(teamName)) {
                sm.registerNewTeam(teamName).setColor(ChatColor.valueOf(teamName.toUpperCase()));
                Bukkit.getLogger().info("Team " + teamName + " created");
            } else {
                Bukkit.getLogger().info("Team " + teamName + " already exists");
            }
        }
    }

    public void deleteTeams() {
        Scoreboard sm = Bukkit.getScoreboardManager().getMainScoreboard();
        // Loops through all teams
        sm.getTeams().forEach(team -> {
            // Deletes all teams that it can find
            team.unregister();
        });
    }

}
