package org.phileasfogg3.limitedLife.Listeners;

import net.nexia.nexiaapi.Config;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MobListener implements Listener {

    private Config gameMgr;

    public MobListener(Config gameMgr) {
        this.gameMgr = gameMgr;
    }

    private final Random random = new Random();

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();

        // Only apply to mobs (ignore players, projectiles, etc.)
        if (!type.isAlive()) return;

        if (random.nextDouble() <= gameMgr.getData().getDouble("spawn-egg-drop-chance")) {
            String eggName = type.name() + "_SPAWN_EGG";
            try {
                Material eggMaterial = Material.valueOf(eggName);
                ItemStack egg = new ItemStack(eggMaterial);
                entity.getWorld().dropItemNaturally(entity.getLocation(), egg);
            } catch (IllegalArgumentException e) {
                // No spawn egg exists for this entity (e.g., WITHER, ENDER_DRAGON)
                System.out.println("No spawn egg exists for " + type.name());
            }
        }
    }

}
