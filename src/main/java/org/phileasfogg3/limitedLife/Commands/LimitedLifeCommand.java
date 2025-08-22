package org.phileasfogg3.limitedLife.Commands;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nexia.nexiaapi.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Listeners.RecipieViewer;
import org.phileasfogg3.limitedLife.Managers.PlayerNameManager;

import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LimitedLifeCommand implements CommandExecutor, TabCompleter {

    private Config playerData;
    private Config gameMgr;
    private Config messagesData;

    private static final List<String> SUBCOMMANDS = Arrays.asList("recipes", "toggle");
    private static final List<String> RECIPE_OPTIONS = Arrays.asList("tnt", "spawner", "enchanting_table", "saddle", "name_tag", "bookshelf");
    private static final List<String> TOGGLE_OPTIONS = Arrays.asList("timer");

    private final RecipieViewer recipieViewer;

    public LimitedLifeCommand(Config playerData, Config gameMgr, Config messagesData) {

        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.messagesData = messagesData;

        this.recipieViewer = new RecipieViewer();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (args.length == 0) {

                // Unknown command, too few arguments

                return true;
            }

            switch (args[0].toLowerCase()) {

                case "recipes":
                    handleRecipe(sender, args);
                    break;
                case "toggle":
                    handleToggle(sender, args);
                    break;
                default:
                    // Unknown command

            }

        }


        return true;
    }

    private void handleRecipe(CommandSender sender, String[] args) {

        if (args.length < 2) {

            // Too few arguments

            return;
        }

        Player player = (Player) sender;

        recipieViewer.openRecipeGUI(player, args[1].toLowerCase());

    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Unknown command. Please specify what you want to toggle.");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "timer":

                if (gameMgr.getData().getBoolean("session-active")) {
                    UUID uuid = player.getUniqueId();
                    boolean enabled = LimitedLife.Instance.timerDisplayToggle.getOrDefault(uuid, false);
                    LimitedLife.Instance.timerDisplayToggle.put(uuid, !enabled);

                    if (enabled) {
                        player.sendMessage(ChatColor.RED + "Timer display off");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Timer display on");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You can't toggle this when the session isn't active!");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "What on earth are you trying to toggle?");
                break;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("limitedlife")) return Collections.emptyList();

        if (args.length == 1) {
            return partialMatch(args[0], SUBCOMMANDS);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "recipes":
                    return partialMatch(args[1], RECIPE_OPTIONS);
                case "toggle":
                    return partialMatch(args[1], TOGGLE_OPTIONS);
            }
        }

        return Collections.emptyList();
    }

    // --- Helper method to match partial strings ---
    private List<String> partialMatch(String arg, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(arg.toLowerCase())) {
                matches.add(option);
            }
        }
        return matches;
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
