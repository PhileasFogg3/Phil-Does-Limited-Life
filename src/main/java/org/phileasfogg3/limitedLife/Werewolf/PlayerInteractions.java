package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.phileasfogg3.limitedLife.LimitedLife;

public class PlayerInteractions {

    final Config playerData;
    final Config gameMgr;
    final Config werewolf;

    final WerewolfManager wmManager = LimitedLife.werewolfManager;

    public PlayerInteractions(Config playerData, Config gameMgr, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.werewolf = werewolf;

    }



}
