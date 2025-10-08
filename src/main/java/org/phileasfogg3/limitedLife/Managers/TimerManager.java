package org.phileasfogg3.limitedLife.Managers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.phileasfogg3.limitedLife.LimitedLife;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TimerManager extends BukkitRunnable {

    private final Player player;
    private final UUID uuid;
    private Config playerData;
    private Config gameMgr;

    public TimerManager(Player player, UUID uuid, Config playerData,Config gameMgr) {
        this.player = player;
        this.uuid = uuid;
        this.playerData = playerData;
        this.gameMgr = gameMgr;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        long timeLeft = LimitedLife.Instance.playerTimes.getOrDefault(uuid, 0L);

        if (timeLeft <= 0) {
            player.sendTitle(ChatColor.RED + "YOU ARE OUT OF TIME", "");
            player.setGameMode(GameMode.SPECTATOR);

            Sound sound = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            float volume = 1.0f;
            float pitch = 1.0f;

            for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                onlinePlayers.playSound(onlinePlayers.getLocation(), sound, volume, pitch);
                onlinePlayers.sendMessage(ChatColor.RED + player.getName() + " has ran out of time!");
            }

            if (gameMgr.getData().getBoolean("specials.DoubleLife.enabled")) {

                SoulmatesManager.markExpired(player);

            }

            PlayerNameManager PNM = new PlayerNameManager(playerData, gameMgr);
            PNM.setTABListTime(player, formatTime(timeLeft), "white");

            cancel();
            return;
        }

        timeLeft--;
        LimitedLife.Instance.playerTimes.put(uuid, timeLeft);

        // Update name color
        PlayerNameManager PNM = new PlayerNameManager(playerData, gameMgr);
        PNM.checkUpdate(timeLeft, player);

        // Show action bar if enabled
        Boolean show = LimitedLife.Instance.timerDisplayToggle.getOrDefault(uuid, false);
        if (show != null && show) {
            String formatted = ChatColor.valueOf(PNM.colorFromTime(timeLeft).toUpperCase()) + "" + ChatColor.valueOf("BOLD") + formatTime(timeLeft);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formatted));
        }
    }

    private String formatTime(long seconds) {
        long hrs = TimeUnit.SECONDS.toHours(seconds);
        long mins = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }
}