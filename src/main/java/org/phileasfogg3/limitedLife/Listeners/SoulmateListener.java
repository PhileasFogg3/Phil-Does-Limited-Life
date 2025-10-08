package org.phileasfogg3.limitedLife.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.phileasfogg3.limitedLife.Managers.SoulmatesManager;
import org.phileasfogg3.limitedLife.Utils.SoulmateLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SoulmateListener implements Listener {

    List<Player> killedByPartner = new ArrayList<>();

    private final Random random = new Random();

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (SoulmatesManager.mirroring) return; // prevent infinite loop

        Player partner = getPartner(victim);
        if (partner == null) return;

        if (victim.getGameMode().equals(GameMode.SURVIVAL)) {

            if (victim.getHealth() - event.getFinalDamage() <= 0) {
                killedByPartner.add(partner);
                System.out.println(partner + " was added to killedByPartner");
            }

            SoulmatesManager.mirroring = true;
            partner.damage(event.getFinalDamage());
            SoulmatesManager.mirroring = false;
        }

    }

    @EventHandler
   public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();
        Player partner = getPartner(victim);
        if (partner == null) return;

        if (killedByPartner.contains(victim)) {
            killedByPartner.remove(victim);

            List<String> deathMessages = new ArrayList<>() {{
                add(String.format("%s died due to %s's stupidity.", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s died due to %s's incompetence.", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s paid the price for %s's mistakes.", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s perished thanks to %s's amazing talent for \"helping\".", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s died because %s's insisted they knew what they were doing.", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s was deleted from existence by %s's curiosity.", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s became collateral damage in %s's master plan.", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s lost their life to %s's heroic attempt at sabotage.", victim.getDisplayName(), partner.getDisplayName()));
                add(String.format("%s was \"accidentally\" sacrificed by %s.", victim.getDisplayName(), partner.getDisplayName()));
            }};

            String deathMessage = deathMessages.get(random.nextInt(deathMessages.size()));
            event.setDeathMessage(deathMessage);
        }

 }

    @EventHandler
public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (SoulmatesManager.mirroring) return; // prevent infinite loop

        Player partner = getPartner(victim);
        if (partner == null) return;

        SoulmatesManager.mirroring = true;

        double newHealth = Math.min(
                partner.getHealth() + event.getAmount(),
                partner.getAttribute(Attribute.MAX_HEALTH).getValue()
        );
        partner.setHealth(newHealth);

        SoulmatesManager.mirroring = false;
    }


    public Player getPartner(Player victim) {

        SoulmateLink link = SoulmatesManager.linkedPlayers.get(victim.getUniqueId());

        if (link == null) {
            return null;
        }

        return Bukkit.getPlayer(link.getPartner());

    }
}
