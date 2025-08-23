package org.phileasfogg3.limitedLife.Werewolf.Commands;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Werewolf.GUIManager;
import org.phileasfogg3.limitedLife.Werewolf.WerewolfLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WerewolfCommand implements CommandExecutor, TabCompleter {

    private Config playerData;
    private Config gameMgr;
    private Config werewolf;

    private static final List<String> SUBCOMMANDS = Arrays.asList("accuse");

    public static boolean accusationInProgress = false;

    public WerewolfCommand(Config playerData, Config gameMgr, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.werewolf = werewolf;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ((sender instanceof Player)) {
            Player player = (Player) sender;

            if (args.length == 0) {

                // Unknown command, too few arguments
                player.sendMessage(ChatColor.RED + "You have entered too few arguments. Please try again!");

                return true;
            }

            switch (args[0].toLowerCase()) {

                case "accuse":
                    if (!accusationInProgress) {
                        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                        players.remove(player);
                        GUIManager.open(player, ChatColor.RED + "Accuse a fellow Villager", players);

                        // TODO REPLACE THIS WITH A PROPER MESSAGE ONCE ITS FINISHED
                        WerewolfLogger WL = new WerewolfLogger(LimitedLife.Instance);
                        WL.log(player.getName() + " is accusing someone of something.");

                        accusationInProgress = true;
                    } else {
                        player.sendMessage(ChatColor.RED + "An accusation is already in progress, you need to wait.");
                    }
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown command: " + args[0]);

            }

        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("werewolf")) return Collections.emptyList();

        if (args.length == 1) {
            return partialMatch(args[0], SUBCOMMANDS);
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
}
