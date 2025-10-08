package org.phileasfogg3.limitedLife.Commands;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Listeners.PlayerListener;
import org.phileasfogg3.limitedLife.Managers.BoogeymenManager;
import org.phileasfogg3.limitedLife.Managers.SessionManager;
import org.phileasfogg3.limitedLife.Managers.SoulmatesManager;
import org.phileasfogg3.limitedLife.Utils.SoulmateLink;

import java.util.*;

public class LimitedLifeAdminCommand implements CommandExecutor, TabCompleter {

    private Config playerData;
    private Config gameMgr;
    private Config messagesData;
    private Config werewolf;

    private static final List<String> BASE_SUBCOMMANDS = Arrays.asList("sessionstart", "cure", "confirm", "resume", "addtime", "subtracttime", "rollboogeymen", "pause", "unpause");
    private static final List<String> SESSION_START = Arrays.asList("");
    private static final List<String> BOOGEY_ROLL_1 = Arrays.asList("<number of boogeymen>");
    private static final List<String> CONFIRM = Arrays.asList("");
    private static final List<String> RESUME = Arrays.asList("");
    private static final List<String> BOOGEY_ROLL_2 = Arrays.asList("<time in seconds until draw>");

    public LimitedLifeAdminCommand(Config playerData, Config gameMgr, Config messagesData, Config werewolf) {

        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.messagesData = messagesData;
        this.werewolf = werewolf;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {

                // Unknown command, too few arguments
                player.sendMessage(ChatColor.RED + "WHAT ON EARTH ARE YOU TRYING TO DO?");
                return true;
            }

            if (player.hasPermission("limitedlife.admin")) {
                switch (args[0].toLowerCase()) {

                    case "sessionstart":
                        SessionManager SM = new SessionManager(gameMgr, playerData, messagesData);
                        boolean sessionActive = gameMgr.getData().getBoolean("session-active");
                        int sessionNumber = gameMgr.getData().getInt("session-information.session-number");
                         if (sessionNumber == 0 && !sessionActive) {
                             // Do this if its the first session
                             SM.sessionStart(sessionNumber);
                             SM.startCountdown();
                             player.sendMessage(ChatColor.GREEN + "Starting session " + sessionNumber);
                         } else if (sessionNumber > 0 && !sessionActive) {
                             // Do this if its not the first session and the session has not already started.
                             SM.sessionStart(sessionNumber);
                             SM.startCountdown();
                             player.sendMessage(ChatColor.GREEN + "Starting session " + sessionNumber);
                         } else if (sessionActive) {
                             // Do this if the session has already started.
                             player.sendMessage(ChatColor.RED + "The session has already started...");
                         }
                        break;
                    case "cure":
                        handleCure(sender, args);
                        break;
                    case "confirm":
                        // Confirms an automatic action taken by the server
                        if (gameMgr.getData().getBoolean("confirmation.required")) {
                            gameMgr.getData().set("confirmation.value", 1);
                            player.sendMessage(ChatColor.GREEN + "Confirmed!");
                        } else {
                            player.sendMessage(ChatColor.RED + "I fear you are mistaken, for there is nothing to confirm... Dum Dum");
                        }
                        break;
                    case "resume":
                        if (gameMgr.getData().getInt("session-information.first-half-progress") == -1 && gameMgr.getData().getInt("session-information.break-progress") == -1 && gameMgr.getData().getInt("session-information.second-half-progress") == -1) {
                            player.sendMessage(ChatColor.RED + "There is nothing to resume... What are you playing at?");
                        } else if (gameMgr.getData().getInt("session-information.first-half-progress") != -1){
                            SessionManager sM = new SessionManager(gameMgr, playerData, messagesData);
                            sM.resumeInitializer(gameMgr.getData().getInt("session-information.first-half-progress"), 1);
                        } else if (gameMgr.getData().getInt("session-information.break-progress") != -1){
                            SessionManager sM = new SessionManager(gameMgr, playerData, messagesData);
                            sM.resumeInitializer(gameMgr.getData().getInt("session-information.break-progress"), 2);
                        } else if (gameMgr.getData().getInt("session-information.second-half-progress") != -1){
                            SessionManager sM = new SessionManager(gameMgr, playerData, messagesData);
                            sM.resumeInitializer(gameMgr.getData().getInt("session-information.second-half-progress"), 3);
                        } else {
                            player.sendMessage(ChatColor.RED + "Something has gone wrong! oopsie. Phil bad");
                        }
                        break;
                    case "addtime":
                        handleAddTime(sender, args);
                        break;
                    case "subtracttime":
                        handleSubtractTime(sender, args);
                        break;
                    case "rollboogeymen":
                        handleBoogeyManRoll(sender, args);
                        break;
                    case "pause":
                        handlePause(sender, args);
                        break;
                    case "unpause":
                        handleUnPause(sender, args);
                        break;
                    case "rollsoulmates":
                        if (gameMgr.getData().getBoolean("specials.DoubleLife.enabled")) {
                            handleRollSoulmates(sender, args);
                        } else {
                            player.sendMessage(ChatColor.RED + "Soulmates are not enabled in this session");
                        }
                        break;
                    case "partner":
                        if (gameMgr.getData().getBoolean("specials.DoubleLife.enabled")) {

                            handlePartner(sender, args);

                        } else {
                            player.sendMessage(ChatColor.RED + "Soulmates are not enabled in this session");
                        }
                        break;
                    default:
                        //Unkown Command
                        player.sendMessage(ChatColor.RED + "WHAT ON EARTH ARE YOU TRYING TO DO?");
                }
            } else {
                // No permission error
                noPermissionRant(player);
            }
        }

        return true;
    }

    private void handleCure(CommandSender sender, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (args.length < 2) {

                    // Unknown command, too few arguments
                    player.sendMessage(ChatColor.RED + "Please specify the player you want to cure.");
                    return;
                }

                String playerName = args[1];
                Player targetPlayer = Bukkit.getServer().getPlayerExact(playerName);

                if (targetPlayer != null && targetPlayer.isOnline() && playerData.getData().getBoolean("players." + targetPlayer.getUniqueId() + ".Boogeyman")) {
                    BoogeymenManager BM = new BoogeymenManager(gameMgr, playerData, messagesData);
                    BM.cureBoogeymen(targetPlayer);
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot cure this player... I sense something is wrong...");
                }

            } else {
                noPermissionRant(player);
            }
        }
    }

    private void handleAddTime(CommandSender sender, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (args.length < 2) {

                    player.sendMessage(ChatColor.RED + "Please specify the player you want to add time to.");

                    return;
                }

                String playerName = args[1];
                Player targetPlayer = Bukkit.getServer().getPlayerExact(playerName);
                Long time = Long.parseLong(args[2]);

                if (targetPlayer != null && targetPlayer.isOnline()) {

                    PlayerListener PL = new PlayerListener(playerData, gameMgr, messagesData, werewolf);
                    PL.addTime(targetPlayer, time);

                    player.sendMessage(ChatColor.GREEN + "Added " + time + " seconds to " + playerName);

                }

            } else {
                noPermissionRant(player);
            }

        }

    }

    private void handleSubtractTime(CommandSender sender, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (args.length < 2) {

                    player.sendMessage(ChatColor.RED + "Please specify the player you want to add time to.");

                    return;
                }

                String playerName = args[1];
                Player targetPlayer = Bukkit.getServer().getPlayerExact(playerName);
                Long time = Long.parseLong(args[2]);

                if (targetPlayer != null && targetPlayer.isOnline()) {

                    PlayerListener PL = new PlayerListener(playerData, gameMgr, messagesData, werewolf);
                    PL.subtractTime(targetPlayer, time);

                    player.sendMessage(ChatColor.GREEN + "Removed " + time + " seconds from " + playerName);

                }

            } else {
                noPermissionRant(player);
            }

        }

    }

    private void handleBoogeyManRoll(CommandSender sender, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify how many Boogeymen there should be");
                    return;
                }

                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Please specify how long it is until the boogeyman draw");
                    return;
                }

                try {
                    int number = Integer.parseInt(args[1]);
                    long time = Long.parseLong(args[2]);

                    BoogeymenManager bM = new BoogeymenManager(gameMgr, playerData, messagesData);
                    bM.selectBoogeymen(number, time);
                    bM.announceBoogeymanDraw(time);

                } catch (NumberFormatException e) {

                    player.sendMessage(ChatColor.RED + "The values must be a number, not: " + args[1] + " " + args[2]);

                }
            } else {
                noPermissionRant(player);
            }
        }
    }

    private void handleRollSoulmates(CommandSender sender, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify how long it is until the soulmate draw");
                    return;
                }

                try {
                    long time = Long.parseLong(args[1]);

                    SoulmatesManager SM = new SoulmatesManager(playerData);
                    SM.selectSoulmates(time);
                    SM.announceSoulmates(time);
                } catch (NumberFormatException e) {

                    player.sendMessage(ChatColor.RED + "The values must be a number, not: " + args[1]);

                }

            } else {
                noPermissionRant(player);
            }
        }
    }

    private void handlePartner(CommandSender sender, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (args.length < 2) {

                    player.sendMessage(ChatColor.RED + "Please specify who you want to check.");
                    return;

                }

                String playerName = args[1];
                Player targetPlayer = Bukkit.getServer().getPlayerExact(playerName);

                SoulmateLink link = SoulmatesManager.linkedPlayers.get(targetPlayer.getUniqueId());

                Player soulmate = Bukkit.getPlayer(link.getPartner());

                if (link.isPartnerExpired()) {

                    player.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "'s partner is " + soulmate.getName() + " (expired)");

                } else {

                    player.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "'s partner is " + soulmate.getName());

                }

            } else {
                noPermissionRant(player);
            }

        }

    }

    private void handlePause(CommandSender sender, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (gameMgr.getData().getBoolean("break-active")) {

                    player.sendMessage(ChatColor.RED + "You cannot pause the break, break time means the game is already paused, silly");

                } else if (!gameMgr.getData().getBoolean("session-active")) {

                    player.sendMessage(ChatColor.RED + "You cannot pause something that has not yet started. Dum Dum");

                } else {

                    SessionManager SM = new SessionManager(gameMgr, playerData, messagesData);
                    SM.stopCountdown();

                    gameMgr.getData().set("break-active", true);
                    gameMgr.save();

                    Bukkit.broadcastMessage(ChatColor.RED + "THE SESSION HAS BEEN PAUSED. GO LOOK AT THE ECLIPSE BUT DON'T STARE AT IT DIRECTLY PLS. GOTTA PROTECT YOUR EYES SO WE CAN PLAY MINECRAFT!");

                }
            } else {
                noPermissionRant(player);
            }
        }
    }

    private void handleUnPause(CommandSender sender, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("limitedlife.admin")) {

                if (!gameMgr.getData().getBoolean("break-active")) {

                    player.sendMessage(ChatColor.RED + "You cannot unpause something that has not been paused!");

                } else if (!gameMgr.getData().getBoolean("session-active")) {

                    player.sendMessage(ChatColor.RED + "You cannot unpause something that has not been started!");

                } else {

                    SessionManager SM = new SessionManager(gameMgr, playerData, messagesData);
                    SM.startCountdown();

                    gameMgr.getData().set("break-active", false);
                    gameMgr.save();

                    Bukkit.broadcastMessage(ChatColor.RED + "THE SESSION HAS BEEN UN-PAUSED. DID YOU MANAGE TO SEE THE ECLIPSE? YES?!!! WOOOOOOOOOHHOOOOOOOO");

                }
            } else {
                noPermissionRant(player);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("limitedlifeadmin")) return Collections.emptyList();

        List<String> subcommands = new ArrayList<>(BASE_SUBCOMMANDS);

        if (gameMgr.getData().getBoolean("specials.DoubleLife.enabled")) {

            subcommands.add("rollsoulmates");
            subcommands.add("partner");
        }

        if (args.length == 1) {
            return partialMatch(args[0], subcommands);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "sessionstart":
                    return partialMatch(args[1], SESSION_START);
                case "cure", "subtracttime", "addtime", "partner":
                    return partialMatch(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                case "confirm":
                    return partialMatch(args[1], CONFIRM);
                case "resume":
                    return partialMatch(args[1], RESUME);
                case "rollboogeymen":
                    return partialMatch(args[1], BOOGEY_ROLL_1);
            }
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "rollboogeymen":
                    return partialMatch(args[2], BOOGEY_ROLL_2);
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

    public void noPermissionRant(Player player) {
        List<String> messages = Arrays.asList(
                ChatColor.RED + "" + ChatColor.BOLD + "⚠️ ERROR: INSUFFICIENT PERMISSION – OR, MORE ACCURATELY, A GROSS OVERESTIMATION OF AUTHORITY ⚠️",

                "",

                ChatColor.YELLOW + "Well, " + ChatColor.BOLD + "well" + ChatColor.RESET + ChatColor.YELLOW + "... " + ChatColor.BOLD + "WELL" + ChatColor.RESET + ChatColor.YELLOW + ". Would you look at that. Someone's feeling brave today.",

                ChatColor.YELLOW + "You, a humble mortal of the server, just attempted to execute a command so far above your pay grade, it's basically orbiting the moon. Did you wake up this morning, look in the mirror, and say to yourself, "
                        + ChatColor.ITALIC + "\"Yes, I am the chosen one\""
                        + ChatColor.RESET + ChatColor.YELLOW + "? Because that’s the only explanation I can come up with for this "
                        + ChatColor.BOLD + "utterly audacious"
                        + ChatColor.RESET + ChatColor.YELLOW + " display of keyboard hubris.",

                ChatColor.RED + "" + ChatColor.BOLD + "Let me be crystal clear: YOU DO NOT HAVE PERMISSION to run that command."
                        + ChatColor.RESET + ChatColor.YELLOW + " Not now, not later, not in some far-off alternate timeline where pigs fly, chickens do taxes, and you suddenly become the server overlord.",

                ChatColor.YELLOW + "Oh sure, maybe you thought, "
                        + ChatColor.ITALIC + "\"Hey, what's the worst that could happen?\""
                        + ChatColor.RESET + ChatColor.YELLOW + " Well, THIS. This message is the worst that could happen. This is the server smacking your hand away from the big red button and saying, "
                        + ChatColor.BOLD + "\"NO. BAD " + player.getName() + "!\"",

                ChatColor.YELLOW + "You see, commands like that are reserved for a very "
                        + ChatColor.BOLD + "elite"
                        + ChatColor.RESET + ChatColor.YELLOW + " class of players. The kind of folks who have ascended the ranks, earned trust, and possibly sold a small part of their soul to the admin gods. Not someone who thinks “sudo” is just a typo for “studio.”",

                ChatColor.YELLOW + "So kindly, with all due respect (which, in this context, is "
                        + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "none"
                        + ChatColor.RESET + ChatColor.YELLOW + "), put that command back where you found it, take a deep breath, and maybe go punch a tree or dig a hole—y'know, something within your clearance level.",

                ChatColor.YELLOW + "And next time you get the urge to pretend you're some sort of command-wielding deity, remember this moment. Remember the rejection. Let it humble you.",

                "",

                ChatColor.RED + "" + ChatColor.BOLD + "Denied. Dismissed. Disrespected.",

                "",

                ChatColor.GRAY + "Thank you for your cooperation. Have a pleasant day."
        );

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= messages.size()) {
                    cancel();
                    return;
                }

                player.sendMessage(messages.get(index));
                index++;
            }
        }.runTaskTimer(LimitedLife.Instance, 0L, 200L); // 20 ticks = 1 second between messages
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
