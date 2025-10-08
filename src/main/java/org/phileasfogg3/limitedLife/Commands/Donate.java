package org.phileasfogg3.limitedLife.Commands;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Listeners.PlayerListener;
import org.phileasfogg3.limitedLife.Managers.SessionManager;
import org.phileasfogg3.limitedLife.Managers.SoulmatesManager;
import org.phileasfogg3.limitedLife.Managers.TimerManager;
import org.phileasfogg3.limitedLife.Utils.SoulmateLink;

import java.util.*;

public class Donate implements CommandExecutor, TabCompleter {

    private Config playerData;
    private Config gameMgr;
    private Config messagesData;
    private Config werewolf;

    private static final List<String> SUBCOMMANDS = Arrays.asList("seconds", "minutes", "hours");


    public Donate(Config playerData, Config gameMgr, Config messagesData, Config werewolf) {

        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.messagesData = messagesData;
        this.werewolf = werewolf;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (gameMgr.getData().getBoolean("specials.DoubleLife.enabled")) {

                if (args.length == 0) {

                    player.sendMessage(ChatColor.RED + "How much time are you trying to donate?");

                    return true;
                }

                SoulmateLink link = SoulmatesManager.linkedPlayers.get(player.getUniqueId());



                if (link.getPartner() != null && (gameMgr.getData().getBoolean("session-active") && !gameMgr.getData().getBoolean("break-active"))) {
                    switch (args[0].toLowerCase()) {

                        case "seconds":
                            handleDonate('s', Long.parseLong(args[1]), player, Bukkit.getPlayer(link.getPartner()));
                            break;
                        case "minutes":
                            handleDonate('m', Long.parseLong(args[1]), player, Bukkit.getPlayer(link.getPartner()));
                            break;
                        case "hours":
                            handleDonate('h', Long.parseLong(args[1]), player, Bukkit.getPlayer(link.getPartner()));
                            break;
                        default:
                            player.sendMessage(ChatColor.RED + "You've done that wrong, silly billy. Try again please.");

                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You can't donate time right now, please try again later. Possible reasons for this are: You do not yet have a soulmate, The break is active or The session has not yet started.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Soulmates are not enabled this week.");
            }
        }

        return true;
    }

    private void handleDonate(char option, long time, Player sender, Player soulmate) {

        String timeType = "";

        long workingTime = 0L;

        switch (option) {
            case 's':
                workingTime = time;
                if (time == 1) {
                    timeType = " second";
                } else {
                    timeType = " seconds";
                }
                break;
            case 'm':
                workingTime = time * 60;
                if (time == 1) {
                    timeType = " minute";
                } else {
                    timeType = " minutes";
                }
                break;
            case 'h':
                workingTime = time * 3600;
                if (time == 1) {
                    timeType = " hour";
                } else {
                    timeType = " hours";
                }
                break;
        }

        if (LimitedLife.Instance.playerTimes.get(sender.getUniqueId()) < time) {
            sender.sendMessage(ChatColor.RED + "You cannot donate more time than you actually have. Phil thanks you for stress testing his code.");
            return;
        }

        if (!soulmate.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Your soulmate is not online.");
            return;
        }

        PlayerListener PL = new PlayerListener(playerData, gameMgr, messagesData, werewolf);

        PL.addTime(soulmate, workingTime);
        PL.subtractTime(sender, workingTime);

        SoulmateLink link = SoulmatesManager.linkedPlayers.get(sender.getUniqueId());

        if (link.isPartnerExpired()) {
            // Special edge case if Partner is expired

            SessionManager SM = new SessionManager(gameMgr, playerData, messagesData);
            SM.startCountdown(soulmate);

            soulmate.teleport(sender);
            soulmate.setGameMode(GameMode.SURVIVAL);

        }

        SoulmatesManager.markNotExpired(soulmate);

        sender.sendMessage(ChatColor.GREEN + "You have given your soulmate " + time + timeType);
        soulmate.sendMessage(ChatColor.GREEN + "Your soulmate has given you " + time + timeType);

    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("donate")) return Collections.emptyList();

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
