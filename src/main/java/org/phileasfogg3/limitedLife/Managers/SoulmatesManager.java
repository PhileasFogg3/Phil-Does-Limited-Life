package org.phileasfogg3.limitedLife.Managers;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.phileasfogg3.limitedLife.LimitedLife;
import org.phileasfogg3.limitedLife.Utils.SoulmateLink;

import java.util.*;

public class SoulmatesManager {

    private final Config playerData;

    public static final Map<UUID, SoulmateLink> linkedPlayers = new HashMap<>();
    public static boolean mirroring = false;

    public SoulmatesManager(Config playerData) {
        this.playerData = playerData;
    }

    public void announceSoulmates(long drawTime) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    onlinePlayers.sendMessage(ChatColor.GREEN + "Soulmates are about to be paired...");
                }
            }
        }.runTaskLater(LimitedLife.Instance,(drawTime-60L) * 20L);
    }

    public void selectSoulmates(long drawTime) {
        new BukkitRunnable() {
            @Override
            public void run() {

                linkAll();

                SoulmatesCountdown();

                // Make sure everyone has full health
                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    double maxHealth = onlinePlayers.getAttribute(Attribute.MAX_HEALTH).getValue();
                    double maxAbsorption = onlinePlayers.getAttribute(Attribute.MAX_ABSORPTION).getValue();
                    onlinePlayers.setHealth(maxHealth);
                    onlinePlayers.setAbsorptionAmount(maxAbsorption);
                }
            }
        }.runTaskLater(LimitedLife.Instance,drawTime * 20L);
    }

    public void SoulmatesCountdown() {
        List<String> messages = Arrays.asList(
                ChatColor.GREEN + "3",
                ChatColor.YELLOW + "2",
                ChatColor.RED + "1",
                ChatColor.GRAY + "Your soulmate is..."
        );

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= messages.size() + 1) { // +1 for the name reveal step
                    cancel();

                    Bukkit.broadcastMessage(ChatColor.GRAY +
                            "You now have a soulmate. Your health bars are shared, but your timers are not. " +
                            "So if one of you die... you both die. Lets hope you don't hate your soulmate :D");

                    Bukkit.broadcastMessage(ChatColor.GRAY +
                            "You can donate time to your soulmate using /donate. If your soulmate has ran out of time, donating to them will bring them back as your minion");

                    return;
                }

                if (index < messages.size()) {
                    // Countdown messages (3, 2, 1, "Your soulmate is...")
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendTitle(messages.get(index), "");
                        onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }
                } else {
                    // Show soulmate name (obfuscated or clear)
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        SoulmateLink link = SoulmatesManager.linkedPlayers.get(p.getUniqueId());
                        if (link == null) continue; // no soulmate, skip

                        Player partner = Bukkit.getPlayer(link.getPartner());
                        if (partner == null) continue;

                        boolean showClear = link.isPartnerExpired();

                        String partnerName = partner.getName();
                        String displayName = showClear ? partnerName
                                : ChatColor.MAGIC + partnerName + ChatColor.RESET;

                        p.sendTitle(displayName, "");
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                }

                index++;
            }
        }.runTaskTimer(LimitedLife.Instance, 0L, 40L);
    }


    public class EligiblePlayers {
        public final List<String> active = new ArrayList<>();
        public final List<String> expired = new ArrayList<>();
    }

    public EligiblePlayers getEligiblePlayers() {
        EligiblePlayers result = new EligiblePlayers();

        playerData.getData().getConfigurationSection("players").getKeys(false).forEach(key -> {
            final boolean isOnline = playerData.getData().getBoolean("players." + key + ".Online");
            final long timeLeft = playerData.getData().getLong("players." + key + ".Time");

            if (!isOnline) return;

            if (timeLeft > 0) {
                result.active.add(key);
            } else {
                result.expired.add(key);
            }
        });

        return result;
    }

    public static class Candidate {
        public final Player player;
        public final boolean expired;
        public Candidate(Player player, boolean expired) {
            this.player = player;
            this.expired = expired;
        }
    }

    public void linkAll() {
        EligiblePlayers eligible = getEligiblePlayers();

        // Build unified list of candidates
        List<Candidate> candidates = new ArrayList<>();

        for (String uuidStr : eligible.active) {
            Player p = Bukkit.getPlayer(UUID.fromString(uuidStr));
            if (p != null) candidates.add(new Candidate(p, false));
        }

        for (String uuidStr : eligible.expired) {
            Player p = Bukkit.getPlayer(UUID.fromString(uuidStr));
            if (p != null) candidates.add(new Candidate(p, true));
        }

        // Shuffle them for randomness
        Collections.shuffle(candidates);

        // Clear old links
        linkedPlayers.clear();

        // Pairing loop
        for (int i = 0; i + 1 < candidates.size(); i += 2) {
            Candidate c1 = candidates.get(i);
            Candidate c2 = candidates.get(i + 1);

            // If both are expired, swap c2 with someone later who is active
            if (c1.expired && c2.expired) {
                boolean swapped = false;
                for (int j = i + 2; j < candidates.size(); j++) {
                    if (!candidates.get(j).expired) {
                        // Swap c2 with the later active candidate
                        Candidate tmp = c2;
                        candidates.set(i + 1, candidates.get(j));
                        candidates.set(j, tmp);
                        c2 = candidates.get(i + 1);
                        swapped = true;
                        break;
                    }
                }

                // If we couldn’t swap, it means only expired players are left
                if (!swapped) {
                    Bukkit.getLogger().warning("Unpaired expired player: " + c1.player.getName());
                    continue;
                }
            }

            // Link them, storing whether each partner is expired
            linkPlayers(c1.player, c2.player, c1.expired, c2.expired);

            System.out.println("Linked " + c1.player.getName() + " (" + (c1.expired ? "expired" : "active") + ")"
                    + " with " + c2.player.getName() + " (" + (c2.expired ? "expired" : "active") + ")");
        }

        // Handle odd leftover
        if (candidates.size() % 2 == 1) {
            Candidate leftover = candidates.get(candidates.size() - 1);
            leftover.player.sendMessage(ChatColor.RED + "You don’t have a soulmate right now.");
        }
    }

    public static void markExpired(Player expiredPlayer) {
        UUID expiredId = expiredPlayer.getUniqueId();
        SoulmateLink link = linkedPlayers.get(expiredId);
        if (link == null) return;

        // Mark this player as expired
        link.setSelfExpired(true);

        // Update partner’s record
        UUID partnerId = link.getPartner();
        SoulmateLink partnerLink = linkedPlayers.get(partnerId);
        if (partnerLink != null) {
            partnerLink.setPartnerExpired(true);
        }
    }

    public static void markNotExpired(Player player) {
        UUID playerId = player.getUniqueId();
        SoulmateLink link = linkedPlayers.get(playerId);
        if (link == null) return;

        // Mark this player as not expired
        link.setSelfExpired(false);

        // Update partner’s record
        UUID partnerId = link.getPartner();
        SoulmateLink partnerLink = linkedPlayers.get(partnerId);
        if (partnerLink != null) {
            partnerLink.setPartnerExpired(false);
        }
    }


    public static void linkPlayers(Player p1, Player p2, boolean p1Expired, boolean p2Expired) {
        // For p1 → store p2 as partner
        linkedPlayers.put(p1.getUniqueId(), new SoulmateLink(
                p2.getUniqueId(),
                p2Expired,   // is my partner expired?
                p1Expired    // am I expired?
        ));

        // For p2 → store p1 as partner
        linkedPlayers.put(p2.getUniqueId(), new SoulmateLink(
                p1.getUniqueId(),
                p1Expired,   // is my partner expired?
                p2Expired    // am I expired?
        ));
    }

    public static Player getActivePartner(Player victim) {
        SoulmateLink link = SoulmatesManager.linkedPlayers.get(victim.getUniqueId());
        if (link == null) return null;

        Player partner = Bukkit.getPlayer(link.getPartner());
        if (partner == null || partner.isDead()) return null;

        // Both must be active
        if (link.isPartnerExpired()) return null;
        SoulmateLink partnerLink = SoulmatesManager.linkedPlayers.get(partner.getUniqueId());
        if (partnerLink == null || partnerLink.isPartnerExpired()) return null;

        return partner;
    }
}
