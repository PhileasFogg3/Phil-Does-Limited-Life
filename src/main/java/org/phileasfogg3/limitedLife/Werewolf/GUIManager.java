package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GUIManager implements Listener {

    private Config playerData;
    private Config gameMgr;
    private Config werewolf;

    public GUIManager(Config playerData, Config gameMgr, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.werewolf = werewolf;
    }

    public static void open(Player viewer, String title, List<Player> players) {
        viewer.openInventory(create(title, players));
    }

    public static Inventory create(String title, List<Player> players) {

        int playerRows = (int) Math.ceil(players.size() / 9.0);
        if (playerRows == 0) playerRows = 1;
        if (playerRows > 5) playerRows = 5;

        int rows = playerRows + 1;
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(null, size, title);

        int placed = 0;
        for (Player p : players) {
            if (placed >= playerRows * 9) break;

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            if (meta != null) {
                meta.setOwningPlayer(p);
                meta.setDisplayName(ChatColor.RED + "Accuse " + ChatColor.GREEN + p.getName());
                List<String> lore = new ArrayList<>();
                switch (title) {
                    case "§cAccuse a fellow Villager":
                        lore.add(ChatColor.GRAY + "Click me to start a vote");
                        lore.add(ChatColor.GRAY + "To banish " + p.getName());
                        lore.add(ChatColor.GRAY + "From the village");
                        break;
                    case "§aSelect a player to Heal":
                        lore.add(ChatColor.GRAY + "Click me to heal " + p.getName() + ".");
                        lore.add(ChatColor.GRAY + "If the werewolves attack them,");
                        lore.add(ChatColor.GRAY + "They will not die tonight.");
                        break;
                    case "§aSelect a player to Murder":
                        lore.add(ChatColor.GRAY + "Click me to murder");
                        lore.add(ChatColor.GRAY + p.getName());
                        break;
                    case "§6Select a player to learn their role":
                        lore.add(ChatColor.GRAY + "Click me to");
                        lore.add(ChatColor.GRAY + "learn about " + p.getName() + ".");
                        lore.add(ChatColor.GRAY + "You will discover their role...");
                        break;
                }
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }

            int row = placed / 9;
            int indexInRow = placed % 9;
            int playersInThisRow = Math.min(players.size() - row * 9, 9);
            int start = (9 - playersInThisRow) / 2;

            inv.setItem(row * 9 + start + indexInRow, skull);
            placed++;
        }

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(ChatColor.RED + "Close");
            List<String> lore = new ArrayList<>();
            switch (title) {
                case "§cAccuse a fellow Villager":
                    lore.add(ChatColor.GRAY + "Click me to close the accusation GUI");
                    break;
                case "§aSelect a player to Heal":
                    lore.add(ChatColor.GRAY + "Click me to close the Heal Player GUI");
                    break;
                case "§aSelect a player to Murder":
                    lore.add(ChatColor.GRAY + "Click me to close the Murder GUI");
                    break;
                case "§6Select a player to learn their role":
                    lore.add(ChatColor.GRAY + "Click me to close the Truth Seeker GUI");
                    break;
            }
            barrierMeta.setLore(lore);
            barrier.setItemMeta(barrierMeta);
        }
        inv.setItem(size - 1, barrier);

        return inv;
    }

    public static void openHeal(Player player) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        open(player, ChatColor.GREEN + "Select a player to Heal", players);
    }

    public void openKill(Player player) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        // Remove players who are werewolves
        List<UUID> werewolves = getWerewolves();
        for (UUID uuid : werewolves) {
            players.remove(Bukkit.getPlayer(uuid));
        }
        open(player, ChatColor.RED + "Select a player to Murder", players);
    }

    public static void openTruth(Player player) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.remove(player);
        open(player, ChatColor.GOLD + "Select a player to learn their role", players);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        String title = e.getView().getTitle();

        System.out.println(title);

        if (!(title.equals(ChatColor.RED + "Accuse a fellow Villager"))) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();

        if (clicked == null) return;

        switch (clicked.getType()) {
            case PLAYER_HEAD:
                System.out.println("Player Head");
                break;
            case BARRIER:
                System.out.println("Barrier");
                e.getWhoClicked().closeInventory();
                break;
            default:
                System.out.println("Something else");
        }

    }

    public List<UUID> getWerewolves() {
        List<UUID> werewolves = new ArrayList<>();

        werewolf.getData().getConfigurationSection("players.").getKeys(false).forEach((key -> {
            String role = werewolf.getData().getString("players." + key + ".Role");
            if (Objects.equals(role, "Werewolf")) {
                werewolves.add(UUID.fromString(key));
            }
        }));
        return werewolves;
    }

}
